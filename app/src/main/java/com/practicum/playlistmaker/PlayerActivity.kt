package com.practicum.playlistmaker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.model.Track



class PlayerActivity : AppCompatActivity(){

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // назад
        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // достаём трек
        track = intent.getSerializableExtra(EXTRA_TRACK) as Track


        // заполняем UI
        binding.titleText.text = track.trackName
        binding.artistText.text = track.artistName
        binding.durationValue.text = track.durationMmSs()


        // опциональные блоки
        with(binding){

            if (track.collectionName.isNullOrBlank()) { albumLabel.visibility = View.GONE; albumValue.visibility = View.GONE }
            else albumValue.text = track.collectionName

            track.releaseYear()?.let { yearValue.text = it }
                ?: run { yearLabel.visibility = View.GONE; yearValue.visibility = View.GONE }

            if (track.primaryGenreName.isNullOrBlank()) { genreLabel.visibility = View.GONE; genreValue.visibility = View.GONE }
            else genreValue.text = track.primaryGenreName


            if (track.country.isNullOrBlank()) { countryLabel.visibility = View.GONE; countryValue.visibility = View.GONE }
            else countryValue.text = track.country


        }

        // обложка 512x512 + скругление
        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.img_placeholder)   // img_placeholder и в night
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(binding.coverImage)




        // Play пока не реализуем (следующий спринт)
        binding.playBtn.isEnabled = true



    }


    companion object {
        const val EXTRA_TRACK = "extra_track"
    }


}