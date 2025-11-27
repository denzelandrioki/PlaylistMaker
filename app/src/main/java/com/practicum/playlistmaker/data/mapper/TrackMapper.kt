package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.dto.TrackDto
import com.practicum.playlistmaker.domain.entity.Track

object TrackMapper {
    fun fromDto(dto: TrackDto): Track = Track(
        trackId         = dto.trackId ?: 0L,
        trackName       = dto.trackName.orEmpty(),
        artistName      = dto.artistName.orEmpty(),
        artworkUrl100   = dto.artworkUrl100,
        trackTimeMillis = dto.trackTimeMillis ?: 0L,  // <-- дефолт
        collectionName  = dto.collectionName,
        releaseDate     = dto.releaseDate,
        primaryGenreName= dto.primaryGenreName,
        country         = dto.country,
        previewUrl      = dto.previewUrl
    )
}
