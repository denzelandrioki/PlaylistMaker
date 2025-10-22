package com.practicum.playlistmaker.model

import android.os.Parcelable
import android.text.format.DateUtils
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
    val country: String? = null, // страна
    val previewUrl: String?
) : Parcelable {

    fun getCoverArtwork(): String? =
        artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")

    fun durationMmSs(): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(trackTimeMillis ?: 0L)

    fun releaseYear(): String? =
        releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

}
