package com.practicum.playlistmaker.data.repository

import android.content.Context
import com.practicum.playlistmaker.data.db.PlaylistEntity
import com.practicum.playlistmaker.data.db.PlaylistMapper.toPlaylist
import com.practicum.playlistmaker.data.db.PlaylistMapper.toPlaylistEmpty
import com.practicum.playlistmaker.data.db.PlaylistTrackMapper.toPlaylistTrackEntity
import com.practicum.playlistmaker.data.db.PlaylistsDao
import com.practicum.playlistmaker.data.db.PlaylistTracksDao
import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.File

class PlaylistsRepositoryImpl(
    private val playlistsDao: PlaylistsDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val context: Context,
) : PlaylistsRepository {

    private val coversDir: File by lazy {
        File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
    }

    override fun getPlaylists(): Flow<List<Playlist>> =
        playlistsDao.getAllPlaylistsWithTracks()
            .distinctUntilChanged()
            .map { list -> list.map { it.toPlaylist() } }

    override suspend fun getPlaylistById(id: Long): Playlist? =
        playlistsDao.getByIdWithTracks(id)?.toPlaylist()

    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long {
        val entity = PlaylistEntity(
            id = 0,
            name = name.trim(),
            description = description.trim(),
            coverPath = null,
        )
        val id = playlistsDao.insert(entity)
        if (!coverUri.isNullOrBlank()) {
            val path = copyCoverToAppStorage(coverUri, id)
            if (path != null) {
                val existing = playlistsDao.getByIdWithTracks(id)?.playlist ?: return id
                playlistsDao.update(existing.copy(coverPath = path))
            }
        }
        return id
    }

    override suspend fun updatePlaylistCover(playlistId: Long, coverPath: String) {
        val withTracks = playlistsDao.getByIdWithTracks(playlistId) ?: return
        playlistsDao.update(withTracks.playlist.copy(coverPath = coverPath))
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        playlistTracksDao.insert(track.toPlaylistTrackEntity(playlist.id))
    }

    private fun copyCoverToAppStorage(uriString: String, playlistId: Long): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { input ->
                val file = File(coversDir, "playlist_$playlistId.jpg")
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
                file.absolutePath
            }
        } catch (_: Exception) {
            null
        }
    }
}
