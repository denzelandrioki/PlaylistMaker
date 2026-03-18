package com.practicum.playlistmaker.data.repository

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.data.db.PlaylistEntity
import com.practicum.playlistmaker.data.db.PlaylistMapper.toPlaylist
import com.practicum.playlistmaker.data.db.PlaylistMapper.toEntity
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
    private val gson: Gson,
    private val context: Context,
) : PlaylistsRepository {

    private val coversDir: File by lazy {
        File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
    }

    override fun getPlaylists(): Flow<List<Playlist>> = playlistsDao.getAllPlaylists()
        .distinctUntilChanged()
        .map { list -> list.map { it.toPlaylist(gson) } }

    override suspend fun getPlaylistById(id: Long): Playlist? =
        playlistsDao.getById(id)?.toPlaylist(gson)

    override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long {
        val entity = PlaylistEntity(
            id = 0,
            name = name.trim(),
            description = description.trim(),
            coverPath = null,
            trackIdsJson = "[]",
            trackCount = 0,
        )
        val id = playlistsDao.insert(entity)
        if (!coverUri.isNullOrBlank()) {
            val path = copyCoverToAppStorage(coverUri, id)
            if (path != null) {
                val existing = playlistsDao.getById(id) ?: return id
                playlistsDao.update(existing.copy(coverPath = path))
            }
        }
        return id
    }

    override suspend fun updatePlaylistCover(playlistId: Long, coverPath: String) {
        val existing = playlistsDao.getById(playlistId) ?: return
        playlistsDao.update(existing.copy(coverPath = coverPath))
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        playlistTracksDao.insert(track.toPlaylistTrackEntity())
        val newIds = (playlist.trackIds + track.trackId).distinct()
        val updated = playlist.copy(
            trackIds = newIds,
            trackCount = newIds.size,
        )
        playlistsDao.update(updated.toEntity(gson))
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
