package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/** Интерактор избранного: добавление, удаление, получение списка (Flow, последние сверху). */
interface FavoritesInteractor {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getFavorites(): Flow<List<Track>>
}

class FavoritesInteractorImpl(
    private val repo: FavoritesRepository
) : FavoritesInteractor {

    override suspend fun addToFavorites(track: Track) = repo.addToFavorites(track)

    override suspend fun removeFromFavorites(track: Track) = repo.removeFromFavorites(track)

    override fun getFavorites(): Flow<List<Track>> = repo.getFavorites()
}
