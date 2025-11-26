package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var track: Track

    private val vm: PlayerViewModel by viewModels { Creator.providePlayerViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        track = intent.getParcelableExtraCompat(EXTRA_TRACK) ?: error("Track extra is missing")
        bindTrackStatic(track)

        vm.ui.observe(this) { ui ->
            binding.progressText.text = ui.positionText
            binding.playBtn.setImageResource(
                if (ui.state == PlayerState.PLAYING) R.drawable.ic_pause_32
                else R.drawable.ic_play_32
            )
        }

        binding.playBtn.setOnClickListener { vm.toggle() }

        vm.prepare(track.previewUrl.orEmpty())
    }

    override fun onPause() {
        super.onPause()
        if (vm.ui.value?.state == PlayerState.PLAYING) vm.toggle()
    }

    // ---- helpers
    private fun bindTrackStatic(t: Track) = with(binding) {
        titleText.text = t.trackName
        artistText.text = t.artistName
        durationValue.text = formatMsInt(t.trackTimeMillis.toInt())

        // альбом
        if (t.collectionName.isNullOrBlank()) { albumLabel.gone(); albumValue.gone() }
        else { albumValue.text = t.collectionName; albumLabel.visible(); albumValue.visible() }

        // год
        val year = t.releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
        if (year.isNullOrBlank()) { yearLabel.gone(); yearValue.gone() }
        else { yearValue.text = year; yearLabel.visible(); yearValue.visible() }

        // жанр
        if (t.primaryGenreName.isNullOrBlank()) { genreLabel.gone(); genreValue.gone() }
        else { genreValue.text = t.primaryGenreName; genreLabel.visible(); genreValue.visible() }

        // страна
        if (t.country.isNullOrBlank()) { countryLabel.gone(); countryValue.gone() }
        else { countryValue.text = t.country; countryLabel.visible(); countryValue.visible() }

        val radius = resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(this@PlayerActivity)
            .load(t.cover512())
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(coverImage)

        progressText.text = "00:00"
    }

    private fun formatMsInt(ms: Int): String =
        java.text.SimpleDateFormat("mm:ss", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(ms)

    private fun android.view.View.visible() { this.visibility = android.view.View.VISIBLE }
    private fun android.view.View.gone() { this.visibility = android.view.View.GONE }

    private fun <T : android.os.Parcelable> android.content.Intent.getParcelableExtraCompat(key: String): T? =
        if (android.os.Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, Track::class.java) as T?
        else @Suppress("DEPRECATION") getParcelableExtra(key)

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
