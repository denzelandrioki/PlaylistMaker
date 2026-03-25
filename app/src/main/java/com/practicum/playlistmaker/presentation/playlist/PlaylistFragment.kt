package com.practicum.playlistmaker.presentation.playlist

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistBinding
import com.practicum.playlistmaker.domain.entity.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModel()

    private val tracksAdapter = PlaylistTracksAdapter(
        onTrackClick = { track ->
            findNavController().navigate(
                R.id.action_playlistFragment_to_playerFragment,
                Bundle().apply { putParcelable("track", track) },
            )
        },
        onTrackLongClick = { track -> showRemoveTrackDialog(track.trackId) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        setupShareButton()
        setupMenuSheet()

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = tracksAdapter

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            tracksAdapter.submitList(tracks)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) return@observe
            val playlist = state.playlist ?: run {
                findNavController().navigateUp()
                return@observe
            }
            binding.playlistTitle.text = playlist.name
            if (playlist.description.isNotBlank()) {
                binding.playlistDescription.isVisible = true
                binding.playlistDescription.text = playlist.description
            } else {
                binding.playlistDescription.isVisible = false
            }
            val durationStr = getString(R.string.playlist_duration_minutes, state.durationMinutes.toInt())
            val tracksStr = resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount,
            )
            binding.playlistStats.text = "$durationStr • $tracksStr"
            if (playlist.coverUri != null) {
                binding.playlistCover.isVisible = true
                binding.playlistCover.setBackgroundResource(0)
                // Очищаем кэш Glide перед загрузкой новой картинки
                Glide.with(this).clear(binding.playlistCover)
                // Загружаем картинку с принудительным обновлением кэша
                Glide.with(this)
                    .load(playlist.coverUri)
                    .centerCrop()
                    .skipMemoryCache(true) // Пропускаем кэш в памяти
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Не кэшируем на диск
                    .into(binding.playlistCover)
            } else {
                binding.playlistCover.isVisible = true
                Glide.with(this).clear(binding.playlistCover)
                binding.playlistCover.setImageResource(R.drawable.img_placeholder)
                binding.playlistCover.setBackgroundResource(R.drawable.cover_placeholder_playlist)
            }
        }

        viewModel.navigateBackAfterDelete.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.consumeNavigateBackAfterDelete()
                findNavController().navigateUp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Перезагружаем плейлист при возврате с экрана редактирования, чтобы отобразить обновлённую обложку
        // Используем post с задержкой для гарантии, что данные обновились в БД
        binding.root.postDelayed({
            viewModel.refreshPlaylist()
        }, 500)
    }

    private fun setupShareButton() {
        binding.shareButton.setOnClickListener { trySharePlaylist() }
    }

    private fun trySharePlaylist() {
        val tracks = viewModel.tracks.value.orEmpty()
        if (tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_share_empty_toast),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        val playlist = viewModel.state.value?.playlist ?: return
        val text = buildShareText(playlist.name, playlist.description, tracks)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.playlist_menu_share)))
    }

    private fun buildShareText(
        playlistName: String,
        description: String,
        tracks: List<Track>,
    ): String {
        val lines = mutableListOf(playlistName)
        if (description.isNotBlank()) lines.add(description)
        lines.add(resources.getQuantityString(R.plurals.tracks_count, tracks.size, tracks.size))
        tracks.forEachIndexed { index, track ->
            val seconds = track.trackTimeMillis / 1000L
            val duration = DateUtils.formatElapsedTime(seconds)
            lines.add("${index + 1}. ${track.artistName} - ${track.trackName} ($duration)")
        }
        return lines.joinToString("\n")
    }

    private fun setupMenuSheet() {
        val menuSheet = binding.playlistMenuSheet
        val overlay = binding.playlistMenuOverlay
        val behavior = BottomSheetBehavior.from(menuSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                overlay.isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // Обновляем меню при изменении state (включая обложку)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (!state.isLoading && state.playlist != null) {
                val playlist = state.playlist
                binding.menuSheetTitle.text = playlist.name
                binding.menuSheetTrackCount.text = resources.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.trackCount,
                    playlist.trackCount,
                )
                if (playlist.coverUri != null) {
                    Glide.with(this).clear(binding.menuSheetCover)
                    Glide.with(this)
                        .load(playlist.coverUri)
                        .centerCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .into(binding.menuSheetCover)
                    binding.menuSheetCover.setBackgroundResource(0)
                } else {
                    Glide.with(this).clear(binding.menuSheetCover)
                    binding.menuSheetCover.setImageResource(R.drawable.img_placeholder)
                    binding.menuSheetCover.setBackgroundResource(R.drawable.cover_placeholder_playlist)
                }
            }
        }

        binding.menuButton.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        overlay.setOnClickListener { behavior.state = BottomSheetBehavior.STATE_HIDDEN }

        binding.menuItemShare.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            trySharePlaylist()
        }
        binding.menuItemEdit.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.state.value?.playlist?.id?.let { id ->
                findNavController().navigate(
                    R.id.action_playlistFragment_to_editPlaylistFragment,
                    Bundle().apply { putLong("playlistId", id) },
                )
            }
        }
        binding.menuItemDelete.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }
    }

    private fun showDeletePlaylistDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.playlist_delete_dialog_title)
            .setMessage(R.string.playlist_delete_dialog_message)
            .setNegativeButton(R.string.playlist_delete_dialog_cancel) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.playlist_delete_dialog_confirm) { d, _ ->
                d.dismiss()
                viewModel.deletePlaylist()
            }
            .show()
        val buttonColor = ContextCompat.getColor(requireContext(), R.color.primary_blue)
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(buttonColor)
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(buttonColor)
    }

    private fun showRemoveTrackDialog(trackId: Long) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.playlist_remove_track_dialog_title)
            .setNegativeButton(R.string.playlist_remove_track_dialog_no) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.playlist_remove_track_dialog_yes) { dialog, _ ->
                dialog.dismiss()
                viewModel.removeTrack(trackId)
            }
            .show()
        val buttonColor = ContextCompat.getColor(requireContext(), R.color.primary_blue)
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(buttonColor)
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(buttonColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
