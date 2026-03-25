package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    fun getTracksByPlaylistId(playlistId: Long): Flow<List<Track>>
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun updatePlaylist(id: Long, name: String, description: String, coverUri: String?)
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun deletePlaylist(id: Long)
}

class PlaylistsInteractorImpl(
    private val repository: PlaylistsRepository,
) : PlaylistsInteractor {

    override fun getPlaylists(): Flow<List<Playlist>> = repository.getPlaylists()
    override suspend fun getPlaylistById(id: Long): Playlist? = repository.getPlaylistById(id)
    override fun getTracksByPlaylistId(playlistId: Long): Flow<List<Track>> =
        repository.getTracksByPlaylistId(playlistId)
    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long =
        repository.createPlaylist(name, description, coverUri)
    override suspend fun updatePlaylist(id: Long, name: String, description: String, coverUri: String?) =
        repository.updatePlaylist(id, name, description, coverUri)
    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) =
        repository.addTrackToPlaylist(track, playlist)
    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) =
        repository.removeTrackFromPlaylist(playlistId, trackId)
    override suspend fun deletePlaylist(id: Long) = repository.deletePlaylist(id)
}
