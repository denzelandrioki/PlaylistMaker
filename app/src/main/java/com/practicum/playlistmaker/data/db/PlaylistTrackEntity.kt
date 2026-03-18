package com.practicum.playlistmaker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Трек, добавленный хотя бы в один плейлист. Поля как у избранного — для отображения без сети.
 */
@Entity(tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    @PrimaryKey
    val trackId: Long,
    val artworkUrl100: String?,
    val trackName: String,
    val artistName: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val trackTimeMillis: Long,
    val previewUrl: String?,
)
