package com.practicum.playlistmaker.model

import android.os.Parcelable
import android.text.format.DateUtils
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,



    // новые поля для плеера
    val collectionName: String? = null,     // альбом (может быть null)
    val releaseDate: String? = null,        // ISO-строка от iTunes, достанем год
    val primaryGenreName: String? = null,   // жанр
    val country: String? = null             // страна
) : Parcelable {

    fun getCoverArtwork(): String? =
        artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")

    fun durationMmSs(): String =
        (trackTimeMillis ?: 0L).let { ms ->
            val m = (ms / 1000) / 60
            val s = (ms / 1000) % 60
            "%02d:%02d".format(m, s)
        }

    fun releaseYear(): String? =
        releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

}
