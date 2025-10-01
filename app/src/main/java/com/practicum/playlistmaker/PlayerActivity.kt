package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.model.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Кнопка «назад»
        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Получаем Parcelable трек (без падений на разных API)
        track = intent.getParcelableExtraCompat(EXTRA_TRACK) ?: run {
            // Нет данных — закрываем экран
            finish()
            return
        }

        bindTrack(track)
    }

    private fun bindTrack(track: Track) = with(binding) {
        // Основные поля
        titleText.text = track.trackName
        artistText.text = track.artistName
        durationValue.text = track.durationMmSs()

        // Альбом (пара лейбл/значение)
        if (track.collectionName.isNullOrBlank()) {
            albumLabel.visibility = View.GONE
            albumValue.visibility = View.GONE
        } else {
            albumLabel.visibility = View.VISIBLE
            albumValue.visibility = View.VISIBLE
            albumValue.text = track.collectionName
        }

        // Год (из releaseDate)
        val year = track.releaseYear()
        if (year.isNullOrBlank()) {
            yearLabel.visibility = View.GONE
            yearValue.visibility = View.GONE
        } else {
            yearLabel.visibility = View.VISIBLE
            yearValue.visibility = View.VISIBLE
            yearValue.text = year
        }

        // Жанр
        if (track.primaryGenreName.isNullOrBlank()) {
            genreLabel.visibility = View.GONE
            genreValue.visibility = View.GONE
        } else {
            genreLabel.visibility = View.VISIBLE
            genreValue.visibility = View.VISIBLE
            genreValue.text = track.primaryGenreName
        }

        // Страна
        if (track.country.isNullOrBlank()) {
            countryLabel.visibility = View.GONE
            countryValue.visibility = View.GONE
        } else {
            countryLabel.visibility = View.VISIBLE
            countryValue.visibility = View.VISIBLE
            countryValue.text = track.country
        }

        // Обложка 512×512 c CenterCrop + скругление
        loadCover(track.getCoverArtwork())

        // Функционал плеера реализуем позже
        playBtn.isEnabled = true
    }

    private fun loadCover(url: String?) {
        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(binding.coverImage)
    }

    /** Удобная cross-API функция получения Parcelable */
    private fun Intent.getParcelableExtraCompat(key: String): Track? =
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getParcelableExtra(key, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key)
        }

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
