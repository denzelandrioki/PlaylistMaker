package com.practicum.playlistmaker.data.db

import com.practicum.playlistmaker.domain.entity.Track

object PlaylistTrackMapper {

    fun Track.toPlaylistTrackEntity(playlistId: Long) = PlaylistTrackEntity(
        playlistId = playlistId,
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
    )
}
