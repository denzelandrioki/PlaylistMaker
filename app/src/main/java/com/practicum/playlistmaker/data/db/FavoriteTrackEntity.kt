package com.practicum.playlistmaker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность Room для хранения избранного трека.
 * Поля достаточны для отображения в списке и на экране «Аудиоплеер» без сетевых запросов.
 */
@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
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
    val addedAt: Long = System.currentTimeMillis(),
)
