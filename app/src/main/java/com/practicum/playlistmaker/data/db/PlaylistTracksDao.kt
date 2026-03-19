package com.practicum.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTracksDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: PlaylistTrackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tracks: List<PlaylistTrackEntity>)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY trackId")
    fun getTracksByPlaylistId(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deleteByPlaylistIdAndTrackId(playlistId: Long, trackId: Long)

    /** Количество плейлистов, в которых есть трек с данным trackId (для проверки «сиротских» треков). */
    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getPlaylistCountForTrack(trackId: Long): Int
}
