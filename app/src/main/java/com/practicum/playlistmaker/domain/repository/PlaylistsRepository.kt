package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {

    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun updatePlaylistCover(playlistId: Long, coverPath: String)
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
}
