package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
}

class PlaylistsInteractorImpl(
    private val repository: PlaylistsRepository,
) : PlaylistsInteractor {

    override fun getPlaylists(): Flow<List<Playlist>> = repository.getPlaylists()
    override suspend fun getPlaylistById(id: Long): Playlist? = repository.getPlaylistById(id)
    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long =
        repository.createPlaylist(name, description, coverUri)
    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) =
        repository.addTrackToPlaylist(track, playlist)
}
