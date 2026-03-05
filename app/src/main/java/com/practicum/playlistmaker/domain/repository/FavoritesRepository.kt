package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.Track
import kotlinx.coroutines.flow.Flow

/** Репозиторий избранных треков: добавление, удаление, получение списка (Flow). */
interface FavoritesRepository {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getFavorites(): Flow<List<Track>>
}
