package com.practicum.playlistmaker.data.db

import android.net.Uri
import com.practicum.playlistmaker.domain.entity.Playlist

object PlaylistMapper {

    fun PlaylistWithTracks.toPlaylist(): Playlist {
        val coverUri = playlist.coverPath?.let { 
            Uri.parse("file://$it")
        }
        return Playlist(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverUri = coverUri,
            trackIds = tracks.map { it.trackId },
            trackCount = tracks.size,
        )
    }

    fun PlaylistEntity.toPlaylistEmpty(trackCount: Int = 0): Playlist = Playlist(
        id = id,
        name = name,
        description = description,
        coverUri = coverPath?.let { Uri.parse("file://$it") },
        trackIds = emptyList(),
        trackCount = trackCount,
    )
}
