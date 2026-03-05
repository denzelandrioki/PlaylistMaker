package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/** Интерактор избранного: добавление, удаление, получение списка (Flow), проверка по trackId. */
interface FavoritesInteractor {
    fun getFavorites(): Flow<List<Track>>
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(trackId: Long)
    suspend fun isFavorite(trackId: Long): Boolean
}

class FavoritesInteractorImpl(
    private val repo: FavoritesRepository
) : FavoritesInteractor {

    override fun getFavorites(): Flow<List<Track>> = repo.getFavorites()
    override suspend fun addToFavorites(track: Track) = repo.addToFavorites(track)
    override suspend fun removeFromFavorites(trackId: Long) = repo.removeFromFavorites(trackId)
    override suspend fun isFavorite(trackId: Long): Boolean = repo.isFavorite(trackId)
}
