package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.db.FavoriteTrackMapper.toEntity
import com.practicum.playlistmaker.data.db.FavoriteTrackMapper.toTrack
import com.practicum.playlistmaker.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoritesRepository {

    override suspend fun addToFavorites(track: Track) {
        dao.insert(track.toEntity())
    }

    override suspend fun removeFromFavorites(track: Track) {
        dao.delete(track.toEntity())
    }

    override fun getFavorites(): Flow<List<Track>> =
        dao.getAllFavorites().map { list -> list.map { it.toTrack() } }
}
