package com.practicum.playlistmaker.presentation.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlayerBinding
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        bindTrack(track)
        viewModel.prepare(track.previewUrl.orEmpty())

        binding.playBtn.setOnClickListener { viewModel.playPause() }

        viewModel.ui.observe(viewLifecycleOwner) { ui ->
            binding.progressText.text = formatMs(ui.progressMs)
            binding.playBtn.setImageResource(
                if (ui.state == PlayerState.PLAYING) R.drawable.ic_pause_32
                else R.drawable.ic_play_32
            )
            if (ui.state == PlayerState.COMPLETED) binding.progressText.text = "00:00"
        }
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
        playBtn.setImageResource(R.drawable.ic_play_32)
        progressText.text = "00:00"
    }

    private fun formatMs(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(ms)

    @Suppress("DEPRECATION")
    private inline fun <reified T : android.os.Parcelable> Bundle.getParcelableCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getParcelable(key, T::class.java)
        else
            getParcelable(key)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
