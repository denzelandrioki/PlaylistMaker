package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.Track
import kotlinx.coroutines.flow.Flow

/** Репозиторий избранных треков: добавление, удаление, получение списка (Flow), проверка по trackId. */
interface FavoritesRepository {
    fun getFavorites(): Flow<List<Track>>
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(trackId: Long)
    suspend fun isFavorite(trackId: Long): Boolean
}
