package com.practicum.playlistmaker.presentation.player

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlayerBinding
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.service.PlayerService
import com.practicum.playlistmaker.util.applySystemBarBottomInsetAsPadding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Экран плеера: привязка к [PlayerService], отображение трека и управление воспроизведением. */
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()

    private var currentTrack: Track? = null

    /** true, если вызывали bindService — нужен парный unbind. */
    private var playerBindRequested: Boolean = false

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* при отказе foreground может не показаться — воспроизведение не блокируем */ }

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerBinder
            val svc = binder.getService()
            viewModel.attachPlayer(svc)
            val url = currentTrack?.previewUrl.orEmpty()
            viewModel.preparePlayback(url)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.detachPlayer()
        }
    }

    private val bottomSheetAdapter = PlaylistsBottomSheetAdapter { playlist ->
        viewModel.addTrackToPlaylist(playlist)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playerToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val track = arguments?.getParcelableCompat<Track>("track")
            ?: error("Track argument is missing")
        currentTrack = track

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ctx = requireContext()
            when {
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED -> Unit
                else -> requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        viewModel.setTrack(track)
        bindTrack(track)

        val bindIntent = Intent(requireContext(), PlayerService::class.java).apply {
            putExtra(PlayerService.EXTRA_PREVIEW_URL, track.previewUrl.orEmpty())
            putExtra(PlayerService.EXTRA_ARTIST_NAME, track.artistName)
            putExtra(PlayerService.EXTRA_TRACK_NAME, track.trackName)
        }
        playerBindRequested = requireContext().bindService(
            bindIntent,
            playerServiceConnection,
            Context.BIND_AUTO_CREATE,
        )

        binding.playBtn.setOnPlaybackToggleListener { viewModel.playPause() }
        binding.favBtn.setOnClickListener { viewModel.onFavoriteClicked() }

        setupAddToPlaylistBottomSheet()

        viewModel.ui.observe(viewLifecycleOwner) { ui ->
            binding.progressText.text = formatMs(ui.progressMs)
            binding.playBtn.setPlaybackPlaying(ui.state == PlayerState.PLAYING)
            binding.favBtn.setImageResource(
                if (ui.isFavorite) R.drawable.ic_fav_button_enabled else R.drawable.ic_fav_button_disabled,
            )
            binding.favBtn.imageTintList = null
            if (ui.state == PlayerState.COMPLETED) binding.progressText.text = "00:00"
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onPlayerFragmentStart()
    }

    override fun onStop() {
        viewModel.onPlayerFragmentStop()
        super.onStop()
    }

    private fun bindTrack(track: Track) = with(binding) {
        titleText.text = track.trackName
        artistText.text = track.artistName
        durationValue.text = formatMs(track.trackTimeMillis.toInt())

        val year = track.releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
        yearLabel.visibility = if (year.isNullOrBlank()) View.GONE else View.VISIBLE
        yearValue.visibility = yearLabel.visibility
        yearValue.text = year

        if (track.collectionName.isNullOrBlank()) {
            albumLabel.visibility = View.GONE
            albumValue.visibility = View.GONE
        } else {
            albumLabel.visibility = View.VISIBLE
            albumValue.visibility = View.VISIBLE
            albumValue.text = track.collectionName
        }

        if (track.primaryGenreName.isNullOrBlank()) {
            genreLabel.visibility = View.GONE
            genreValue.visibility = View.GONE
        } else {
            genreLabel.visibility = View.VISIBLE
            genreValue.visibility = View.VISIBLE
            genreValue.text = track.primaryGenreName
        }

        if (track.country.isNullOrBlank()) {
            countryLabel.visibility = View.GONE
            countryValue.visibility = View.GONE
        } else {
            countryLabel.visibility = View.VISIBLE
            countryValue.visibility = View.VISIBLE
            countryValue.text = track.country
        }

        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this@PlayerFragment)
            .load(track.cover512())
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(coverImage)

        playBtn.isEnabled = true
        playBtn.setPlaybackPlaying(false)
        progressText.text = "00:00"
    }

    private fun setupAddToPlaylistBottomSheet() {
        val bottomSheet = binding.playlistsBottomSheet
        bottomSheet.applySystemBarBottomInsetAsPadding()
        val overlay = binding.overlay
        val behavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                overlay.visibility = when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> View.GONE
                    else -> View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.addToPlaylistBtn.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.newPlaylistFromPlayerBtn.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            val bundle = Bundle().apply {
                currentTrack?.let { putParcelable("trackToAdd", it) }
            }
            findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment, bundle)
        }

        binding.playlistsBottomSheetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsBottomSheetRecyclerView.adapter = bottomSheetAdapter

        viewModel.playlistsForSheet.observe(viewLifecycleOwner) { list ->
            bottomSheetAdapter.submitList(list)
        }

        viewModel.addToPlaylistResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            when (result) {
                is PlayerAddToPlaylistResult.Added -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.track_added_to_playlist_toast, result.playlistName),
                        Toast.LENGTH_SHORT,
                    ).show()
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                is PlayerAddToPlaylistResult.AlreadyIn -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.track_already_in_playlist_toast, result.playlistName),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
            viewModel.consumeAddToPlaylistResult()
        }
    }

    private fun formatMs(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(ms)

    @Suppress("DEPRECATION")
    private inline fun <reified T : android.os.Parcelable> Bundle.getParcelableCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, T::class.java)
        } else {
            getParcelable(key)
        }

    override fun onDestroyView() {
        if (playerBindRequested) {
            runCatching { requireContext().unbindService(playerServiceConnection) }
            playerBindRequested = false
        }
        viewModel.detachPlayer()
        super.onDestroyView()
    }
}
