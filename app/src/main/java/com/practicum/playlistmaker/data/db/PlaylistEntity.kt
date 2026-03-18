package com.practicum.playlistmaker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverPath: String?,
    /** Список trackId в формате JSON (Gson). */
    val trackIdsJson: String,
    val trackCount: Int,
)
