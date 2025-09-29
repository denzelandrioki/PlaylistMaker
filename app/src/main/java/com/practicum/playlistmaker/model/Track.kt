package com.practicum.playlistmaker.model

import android.text.format.DateUtils
import java.io.Serializable

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
) : Serializable {

    // mm:ss из миллисекунд
    fun durationMmSs(): String = DateUtils.formatElapsedTime(trackTimeMillis / 1000)

    // 512x512 для плеера
    fun getCoverArtwork(): String = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

    // "1965" из "1965-08-23T07:00:00Z"
    fun releaseYear(): String? = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

}
