package com.practicum.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getByIdWithTracks(id: Long): PlaylistWithTracks?

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY id DESC")
    fun getAllPlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: Long)
}
