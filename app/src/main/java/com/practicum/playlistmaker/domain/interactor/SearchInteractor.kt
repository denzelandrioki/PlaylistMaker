package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow

/** Слой между UI и TracksRepository: поиск возвращает Flow, история (suspend) и добавление в историю. */
interface SearchInteractor {
    fun search(query: String): Flow<Result<List<Track>>>
    suspend fun history(): List<Track>
    fun pushToHistory(track: Track)
    fun clearHistory()
}

class SearchInteractorImpl(
    private val repo: TracksRepository
) : SearchInteractor {

    override fun search(query: String): Flow<Result<List<Track>>> = repo.search(query)

    override suspend fun history(): List<Track> = repo.getHistory()

    override fun pushToHistory(track: Track) = repo.addToHistory(track)

    override fun clearHistory() = repo.clearHistory()
}