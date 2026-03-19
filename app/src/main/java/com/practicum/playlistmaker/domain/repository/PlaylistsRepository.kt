package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {

    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    fun getTracksByPlaylistId(playlistId: Long): Flow<List<Track>>
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun updatePlaylist(id: Long, name: String, description: String, coverUri: String?)
    suspend fun updatePlaylistCover(playlistId: Long, coverPath: String)
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun deletePlaylist(id: Long)
}
