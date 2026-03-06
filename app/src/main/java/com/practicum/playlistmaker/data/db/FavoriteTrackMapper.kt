package com.practicum.playlistmaker.data.db

import com.practicum.playlistmaker.domain.entity.Track

object FavoriteTrackMapper {

    fun Track.toEntity(addedAt: Long = System.currentTimeMillis()) = FavoriteTrackEntity(
        trackId = trackId,
        artworkUrl100 = artworkUrl100,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        trackTimeMillis = trackTimeMillis,
        previewUrl = previewUrl,
        addedAt = addedAt,
    )

    fun FavoriteTrackEntity.toTrack() = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        artworkUrl100 = artworkUrl100,
        trackTimeMillis = trackTimeMillis,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
        isFavorite = true,
    )
}
