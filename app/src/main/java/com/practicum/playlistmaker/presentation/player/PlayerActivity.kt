package com.practicum.playlistmaker.presentation.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()
    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        track = intent.getParcelableExtraCompat(EXTRA_TRACK) ?: error("Track extra is missing")

        bindTrack(track)
        viewModel.prepare(track.previewUrl.orEmpty())

        binding.playBtn.setOnClickListener { viewModel.playPause() }

        viewModel.ui.observe(this) { ui ->
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
        durationValue.text = formatMs((track.trackTimeMillis ?: 0L).toInt())

        val year = track.releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
        yearLabel.visibility = if (year.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
        yearValue.visibility = yearLabel.visibility
        yearValue.text = year

        if (track.collectionName.isNullOrBlank()) {
            albumLabel.visibility = android.view.View.GONE
            albumValue.visibility = android.view.View.GONE
        } else {
            albumLabel.visibility = android.view.View.VISIBLE
            albumValue.visibility = android.view.View.VISIBLE
            albumValue.text = track.collectionName
        }

        if (track.primaryGenreName.isNullOrBlank()) {
            genreLabel.visibility = android.view.View.GONE
            genreValue.visibility = android.view.View.GONE
        } else {
            genreLabel.visibility = android.view.View.VISIBLE
            genreValue.visibility = android.view.View.VISIBLE
            genreValue.text = track.primaryGenreName
        }

        if (track.country.isNullOrBlank()) {
            countryLabel.visibility = android.view.View.GONE
            countryValue.visibility = android.view.View.GONE
        } else {
            countryLabel.visibility = android.view.View.VISIBLE
            countryValue.visibility = android.view.View.VISIBLE
            countryValue.text = track.country
        }

        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this@PlayerActivity)
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
    private fun <T : android.os.Parcelable> Intent.getParcelableExtraCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getParcelableExtra(key, Track::class.java) as T?
        else
            getParcelableExtra(key)

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}