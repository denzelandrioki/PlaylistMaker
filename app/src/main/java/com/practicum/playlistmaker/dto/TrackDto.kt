package com.practicum.playlistmaker.dto

import com.practicum.playlistmaker.model.Track
import java.text.SimpleDateFormat
import java.util.Locale

data class TrackDto(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
)

fun TrackDto.toDomain(): Track? {
    // Игнорируем треки без обязательных данных
    if (trackName.isNullOrEmpty() || artistName.isNullOrEmpty() || trackTimeMillis == null) return null
    return Track(
        trackName = trackName,
        artistName = artistName,
        trackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis),
        artworkUrl100 = artworkUrl100 ?: ""
    )
}