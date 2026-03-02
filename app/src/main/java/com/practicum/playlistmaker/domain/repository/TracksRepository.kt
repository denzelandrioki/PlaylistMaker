package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.Track
import kotlinx.coroutines.flow.Flow

/** Контракт: поиск треков (сеть) и локальная история поиска. Поиск возвращает Flow. */
interface TracksRepository {
    fun search(query: String): Flow<Result<List<Track>>>
    fun getHistory(): List<Track>
    fun addToHistory(track: Track)
    fun clearHistory()
}