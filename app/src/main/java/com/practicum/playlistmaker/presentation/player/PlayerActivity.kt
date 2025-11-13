package com.practicum.playlistmaker.presentation.player

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.app.Creator
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track

    private val player by lazy { Creator.playerInteractor() }

    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }
    private var progressRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        track = intent.getParcelableExtraCompat(EXTRA_TRACK) ?: error("Track extra is missing")

        bindTrack(track)

        player.prepare(
            url = track.previewUrl.orEmpty(),
            onPrepared = { /* отрывок 30с — длительность не нужна */ },
            onComplete = { onCompletion() },
            onError = { onCompletion() } // на ошибке сбрасываем UI
        )

        binding.playBtn.setOnClickListener {
            when (player.state()) {
                PlayerState.PLAYING   -> pause()
                PlayerState.PAUSED,
                PlayerState.PREPARED,
                PlayerState.COMPLETED -> play()
                PlayerState.IDLE,
                PlayerState.ERROR     -> play()  // делаем попытку воспроизведения
            }
        }
    }

    private fun bindTrack(track: Track) = with(binding) {
        titleText.text = track.trackName
        artistText.text = track.artistName
        // Long -> Int
        durationValue.text = formatMs((track.trackTimeMillis ?: 0L).toInt())

        // Альбом
        if (track.collectionName.isNullOrBlank()) {
            albumLabel.visibility = android.view.View.GONE
            albumValue.visibility = android.view.View.GONE
        } else {
            albumValue.text = track.collectionName
            albumLabel.visibility = android.view.View.VISIBLE
            albumValue.visibility = android.view.View.VISIBLE
        }

        // Год
        val year = track.releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
        if (year.isNullOrBlank()) {
            yearLabel.visibility = android.view.View.GONE
            yearValue.visibility = android.view.View.GONE
        } else {
            yearValue.text = year
            yearLabel.visibility = android.view.View.VISIBLE
            yearValue.visibility = android.view.View.VISIBLE
        }

        // Жанр
        if (track.primaryGenreName.isNullOrBlank()) {
            genreLabel.visibility = android.view.View.GONE
            genreValue.visibility = android.view.View.GONE
        } else {
            genreValue.text = track.primaryGenreName
            genreLabel.visibility = android.view.View.VISIBLE
            genreValue.visibility = android.view.View.VISIBLE
        }

        // Страна
        if (track.country.isNullOrBlank()) {
            countryLabel.visibility = android.view.View.GONE
            countryValue.visibility = android.view.View.GONE
        } else {
            countryValue.text = track.country
            countryLabel.visibility = android.view.View.VISIBLE
            countryValue.visibility = android.view.View.VISIBLE
        }

        // Обложка 512×512 + скругления
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

    private fun play() {
        player.play()
        binding.playBtn.setImageResource(R.drawable.ic_pause_32)
        startProgressTicker()
    }

    private fun pause() {
        player.pause()
        binding.playBtn.setImageResource(R.drawable.ic_play_32)
        stopProgressTicker()
    }

    private fun onCompletion() {
        stopProgressTicker()
        binding.playBtn.setImageResource(R.drawable.ic_play_32)
        binding.progressText.text = "00:00"
    }

    // ——— Тикер прогресса ———
    private fun startProgressTicker() {
        stopProgressTicker()
        progressRunnable = object : Runnable {
            override fun run() {
                if (player.state() == PlayerState.PLAYING) {
                    binding.progressText.text = formatMs(player.currentPositionMs())
                    uiHandler.postDelayed(this, PROGRESS_TICK_MS)
                }
            }
        }.also { uiHandler.post(it) }
    }

    private fun stopProgressTicker() {
        progressRunnable?.let { uiHandler.removeCallbacks(it) }
        progressRunnable = null
    }

    override fun onPause() {
        super.onPause()
        if (player.state() == PlayerState.PLAYING) pause()
    }

    override fun onDestroy() {
        stopProgressTicker()
        player.stop()
        super.onDestroy()
    }

    // ——— helpers ———
    private fun formatMs(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(ms)

    private fun Intent.getParcelableExtraCompat(key: String): Track? =
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getParcelableExtra(key, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key)
        }

    companion object {
        const val EXTRA_TRACK = "extra_track"
        private const val PROGRESS_TICK_MS = 333L
    }
}