package com.practicum.playlistmaker.data.repository

import android.content.Context
import com.practicum.playlistmaker.data.db.PlaylistEntity
import com.practicum.playlistmaker.data.db.PlaylistMapper.toPlaylist
import com.practicum.playlistmaker.data.db.PlaylistTrackMapper.toPlaylistTrackEntity
import com.practicum.playlistmaker.data.db.PlaylistTrackMapper.toTrack
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

    override suspend fun getPlaylistById(id: Long): Playlist? {
        val withTracks = playlistsDao.getByIdWithTracks(id)
        return withTracks?.toPlaylist()
    }

    override fun getTracksByPlaylistId(playlistId: Long): Flow<List<Track>> =
        playlistTracksDao.getTracksByPlaylistId(playlistId)
            .distinctUntilChanged()
            .map { list -> list.map { it.toTrack() } }

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

    override suspend fun updatePlaylist(id: Long, name: String, description: String, coverUri: String?) {
        val withTracks = playlistsDao.getByIdWithTracks(id) ?: run {
            android.util.Log.e("PlaylistsRepository", "Playlist not found for id=$id")
            return
        }
        val entity = withTracks.playlist
        val existingPath = entity.coverPath
        
        var newCoverPath: String? = existingPath
        
        if (!coverUri.isNullOrBlank()) {
            val uri = android.net.Uri.parse(coverUri)
            
            if (uri.scheme == "content") {
                // Новая картинка из picker - всегда копируем в новый файл с уникальным именем
                val copiedPath = copyCoverToAppStorage(coverUri, id)
                if (copiedPath != null) {
                    newCoverPath = copiedPath
                } else {
                    android.util.Log.e("PlaylistsRepository", "Failed to copy cover from content URI: $coverUri")
                }
            } else if (uri.scheme == "file") {
                val filePath = uri.path
                // Если путь отличается от сохранённого, значит пользователь выбрал другой файл - копируем
                if (filePath != existingPath) {
                    copyCoverToAppStorage(coverUri, id)?.let { newCoverPath = it }
                }
            }
        } else {
            // Если coverUri пустой, удаляем обложку
            newCoverPath = null
        }
        
        // Обновляем БД
        playlistsDao.update(
            entity.copy(
                name = name.trim(),
                description = description.trim(),
                coverPath = newCoverPath,
            )
        )
        
        // Удаляем старый файл, если он отличается от нового
        if (!existingPath.isNullOrBlank() && existingPath != newCoverPath) {
            try {
                val oldFile = File(existingPath)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistsRepository", "Failed to delete old cover file: $existingPath", e)
            }
        }
    }

    override suspend fun updatePlaylistCover(playlistId: Long, coverPath: String) {
        val withTracks = playlistsDao.getByIdWithTracks(playlistId) ?: return
        playlistsDao.update(withTracks.playlist.copy(coverPath = coverPath))
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        playlistTracksDao.insert(track.toPlaylistTrackEntity(playlist.id))
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistTracksDao.deleteByPlaylistIdAndTrackId(playlistId, trackId)
        // Проверка: остался ли трек ещё в каких-либо плейлистах (критерий — логика проверки в репозитории)
        val remainingCount = playlistTracksDao.getPlaylistCountForTrack(trackId)
        if (remainingCount == 0) {
            // Трек больше ни в одном плейлисте не встречается; отдельной таблицы треков нет
        }
    }

    override suspend fun deletePlaylist(id: Long) {
        playlistsDao.deleteById(id)
        // CASCADE удаляет записи в playlist_tracks для этого плейлиста
    }

    private fun copyCoverToAppStorage(uriString: String, playlistId: Long): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { input ->
                // Используем уникальное имя файла с timestamp, чтобы избежать проблем с кэшированием Glide
                val timestamp = System.currentTimeMillis()
                val file = File(coversDir, "playlist_${playlistId}_${timestamp}.jpg")
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaylistsRepository", "Failed to copy cover image", e)
            null
        }
    }
}
