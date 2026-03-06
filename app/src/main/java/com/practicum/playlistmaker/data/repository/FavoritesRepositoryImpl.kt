package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.db.FavoriteTrackMapper.toEntity
import com.practicum.playlistmaker.data.db.FavoriteTrackMapper.toTrack
import com.practicum.playlistmaker.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoritesRepository {

    override fun getFavorites(): Flow<List<Track>> = dao.getAllFavorites()
        .distinctUntilChanged()
        .map { list -> list.map { it.toTrack() } }

    override suspend fun addToFavorites(track: Track) {
        dao.insert(track.toEntity(addedAt = System.currentTimeMillis()))
    }

    override suspend fun removeFromFavorites(trackId: Long) {
        dao.deleteByTrackId(trackId)
    }

    override suspend fun isFavorite(trackId: Long): Boolean = dao.isFavorite(trackId) == true
}
