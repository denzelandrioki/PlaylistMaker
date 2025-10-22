package com.practicum.playlistmaker

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.model.Track
import java.text.SimpleDateFormat
import java.util.*

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track

    private var mediaPlayer: MediaPlayer? = null

    private enum class State { DEFAULT, PREPARED, PLAYING, PAUSED }
    private var state: State = State.DEFAULT

    private val uiHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (state == State.PLAYING) {
                binding.progressText.text = formatMs(mediaPlayer?.currentPosition ?: 0)
                // обновляем 3 раза в секунду
                uiHandler.postDelayed(this, 333L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerToolbar.setNavigationOnClickListener {
            stopAndRelease()
            onBackPressedDispatcher.onBackPressed()
        }

        // получаем трек (Parcelable для всех API)
        track = intent.getParcelableExtraCompat(EXTRA_TRACK)
            ?: error("PlayerActivity: Track extra is missing")

        bindTrack(track)
        preparePlayer(track.previewUrl)

        binding.playBtn.setOnClickListener {
            when (state) {
                State.PREPARED, State.PAUSED -> startPlayback()
                State.PLAYING -> pausePlayback()
                else -> Unit
            }
        }
    }

    // ——— Жизненный цикл: пауза при уходе в фон ———
    override fun onStop() {
        super.onStop()
        if (state == State.PLAYING) pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        stopAndRelease()
    }

    // ——— Подготовка плеера ———
    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) {
            binding.playBtn.isEnabled = false
            return
        }
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(previewUrl)
            setOnPreparedListener {
                state = State.PREPARED
                binding.playBtn.isEnabled = true
                binding.progressText.text = formatMs(0)
            }
            setOnCompletionListener {
                // Закончили: сброс в исходное состояние
                state = State.PREPARED
                setPlayIcon()
                stopTimer()
                binding.progressText.text = formatMs(0)
                seekTo(0) // на начало
            }
            prepareAsync()
        }
        binding.playBtn.isEnabled = false
    }

    // ——— Управление воспроизведением ———
    private fun startPlayback() {
        mediaPlayer?.start()
        state = State.PLAYING
        setPauseIcon()
        startTimer()
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        state = State.PAUSED
        setPlayIcon()
        stopTimer()
    }

    private fun stopAndRelease() {
        mediaPlayer?.run {
            stopTimer()
            try { stop() } catch (_: Throwable) {}
            release()
        }
        mediaPlayer = null
        state = State.DEFAULT
    }

    // ——— Таймер прогресса ———
    private fun startTimer() {
        uiHandler.removeCallbacks(progressRunnable)
        uiHandler.post(progressRunnable)
    }

    private fun stopTimer() {
        uiHandler.removeCallbacks(progressRunnable)
    }

    // ——— Привязка данных к UI ———
    private fun bindTrack(t: Track) = with(binding) {
        titleText.text = t.trackName
        artistText.text = t.artistName
        durationValue.text = t.durationMmSs()

        if (t.collectionName.isNullOrBlank()) {
            albumLabel.visibility = View.GONE; albumValue.visibility = View.GONE
        } else {
            albumLabel.visibility = View.VISIBLE
            albumValue.visibility = View.VISIBLE
            albumValue.text = t.collectionName
        }

        val year = t.releaseYear()
        if (year == null) {
            yearLabel.visibility = View.GONE; yearValue.visibility = View.GONE
        } else {
            yearLabel.visibility = View.VISIBLE; yearValue.visibility = View.VISIBLE
            yearValue.text = year
        }

        if (t.primaryGenreName.isNullOrBlank()) {
            genreLabel.visibility = View.GONE; genreValue.visibility = View.GONE
        } else {
            genreLabel.visibility = View.VISIBLE; genreValue.visibility = View.VISIBLE
            genreValue.text = t.primaryGenreName
        }

        if (t.country.isNullOrBlank()) {
            countryLabel.visibility = View.GONE; countryValue.visibility = View.GONE
        } else {
            countryLabel.visibility = View.VISIBLE; countryValue.visibility = View.VISIBLE
            countryValue.text = t.country
        }

        // обложка
        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this@PlayerActivity)
            .load(t.getCoverArtwork())
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(coverImage)

        progressText.text = formatMs(0)
        setPlayIcon()
        playBtn.isEnabled = false // включится после prepare()
    }

    // ——— Вспомогательные ———
    private fun setPlayIcon() { binding.playBtn.setImageResource(R.drawable.ic_play_32) }
    private fun setPauseIcon() { binding.playBtn.setImageResource(R.drawable.ic_pause_32) }

    private fun formatMs(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(ms)

    private fun Intent.getParcelableExtraCompat(key: String): Track? =
        if (Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, Track::class.java)
        else @Suppress("DEPRECATION") getParcelableExtra(key)

    companion object { const val EXTRA_TRACK = "extra_track" }
}
