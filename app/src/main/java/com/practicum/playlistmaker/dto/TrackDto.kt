package com.practicum.playlistmaker.dto

import com.practicum.playlistmaker.model.Track
import java.text.SimpleDateFormat
import java.util.Locale

data class TrackDto(
    val trackId: Long?,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,        // iTunes ISO
    val primaryGenreName: String?,
    val country: String?
)

fun TrackDto.toDomain(): Track? {
    // Игнорируем треки без обязательных данных
    if (trackId == null || trackName.isNullOrEmpty() || artistName.isNullOrEmpty() || trackTimeMillis == null) return null
    return Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100.orEmpty(),
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country
    )
}