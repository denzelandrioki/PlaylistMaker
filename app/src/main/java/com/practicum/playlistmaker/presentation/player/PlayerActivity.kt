package com.practicum.playlistmaker.presentation.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.app.Creator
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.domain.entity.Track
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel by viewModels<PlayerViewModel> { Creator.providePlayerViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val track: Track = intent.getParcelableExtraCompat(EXTRA_TRACK)
            ?: error("Track extra is missing")

        bindStatic(track)
        viewModel.init(track)

        // click
        binding.playBtn.setOnClickListener { viewModel.onPlayPauseClicked() }

        // observe UI state
        viewModel.ui.observe(this) { state ->
            // кнопка
            binding.playBtn.isEnabled = state.canPlay || state.canPause
            binding.playBtn.setImageResource(
                if (state.canPause) R.drawable.ic_pause_32 else R.drawable.ic_play_32
            )
            // прогресс
            binding.progressText.text = formatMs(state.progressMs)
        }
    }

    private fun bindStatic(track: Track) = with(binding) {
        titleText.text = track.trackName
        artistText.text = track.artistName
        durationValue.text = formatMs((track.trackTimeMillis ?: 0L).toInt())

        // Альбом
        if (track.collectionName.isNullOrBlank()) {
            albumLabel.gone(); albumValue.gone()
        } else {
            albumValue.text = track.collectionName
            albumLabel.visible(); albumValue.visible()
        }

        // Год
        val year = track.releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
        if (year.isNullOrBlank()) {
            yearLabel.gone(); yearValue.gone()
        } else {
            yearValue.text = year
            yearLabel.visible(); yearValue.visible()
        }

        // Жанр
        if (track.primaryGenreName.isNullOrBlank()) {
            genreLabel.gone(); genreValue.gone()
        } else {
            genreValue.text = track.primaryGenreName
            genreLabel.visible(); genreValue.visible()
        }

        // Страна
        if (track.country.isNullOrBlank()) {
            countryLabel.gone(); countryValue.gone()
        } else {
            countryValue.text = track.country
            countryLabel.visible(); countryValue.visible()
        }

        // Обложка 512×512
        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this@PlayerActivity)
            .load(track.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(coverImage)

        playBtn.isEnabled = true
        playBtn.setImageResource(R.drawable.ic_play_32)
        progressText.text = "00:00"
    }

    // helpers
    private fun formatMs(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(ms)

    // ↓ корректный импорт Build и использование константы API
    private fun <T : android.os.Parcelable> Intent.getParcelableExtraCompat(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            @Suppress("UNCHECKED_CAST")
            getParcelableExtra(key, Track::class.java) as T?
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key)
        }

    private fun android.view.View.gone() { this.visibility = android.view.View.GONE }
    private fun android.view.View.visible() { this.visibility = android.view.View.VISIBLE }

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
