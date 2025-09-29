package com.practicum.playlistmaker

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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

        // Кнопка "назад"
        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Получаем трек из Intent (совместимо с API < 33 и >= 33)
        val incoming: Track? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TRACK)
        }

        if (incoming == null) {
            // Если по какой-то причине данных нет — закрываем экран аккуратно
            finish()
            return
        }
        track = incoming

        bindTrack(track)
    }

    private fun bindTrack(track: Track) = with(binding) {
        // Текстовые поля
        titleText.text = track.trackName
        artistText.text = track.artistName
        durationValue.text = track.durationMmSs()

        // Альбом (если нет — скрываем пару "лейбл/значение")
        if (track.collectionName.isNullOrBlank()) {
            albumLabel.visibility = View.GONE
            albumValue.visibility = View.GONE
        } else {
            albumValue.text = track.collectionName
            albumLabel.visibility = View.VISIBLE
            albumValue.visibility = View.VISIBLE
        }

        // Год (releaseDate -> год или скрыть)
        val year = track.releaseYear()
        if (year.isNullOrBlank()) {
            yearLabel.visibility = View.GONE
            yearValue.visibility = View.GONE
        } else {
            yearValue.text = year
            yearLabel.visibility = View.VISIBLE
            yearValue.visibility = View.VISIBLE
        }

        // Жанр
        if (track.primaryGenreName.isNullOrBlank()) {
            genreLabel.visibility = View.GONE
            genreValue.visibility = View.GONE
        } else {
            genreValue.text = track.primaryGenreName
            genreLabel.visibility = View.VISIBLE
            genreValue.visibility = View.VISIBLE
        }

        // Страна
        if (track.country.isNullOrBlank()) {
            countryLabel.visibility = View.GONE
            countryValue.visibility = View.GONE
        } else {
            countryValue.text = track.country
            countryLabel.visibility = View.VISIBLE
            countryValue.visibility = View.VISIBLE
        }

        // Обложка 512×512 (или плейсхолдер)
        loadCover(track.getCoverArtwork())

        // Кнопку Play пока оставляем активной (реализация — в следующем спринте)
        playBtn.isEnabled = true
    }

    private fun loadCover(url: String?) {
        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(binding.coverImage)
    }

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
