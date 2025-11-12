package com.practicum.playlistmaker.domain.interactor


import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository

interface SearchInteractor {
    fun search(query: String, callback: (Result<List<Track>>) -> Unit)
    fun history(): List<Track>
    fun pushToHistory(track: Track)
    fun clearHistory()
}

class SearchInteractorImpl(
    private val repo: TracksRepository
) : SearchInteractor {

    override fun search(query: String, callback: (Result<List<Track>>) -> Unit) =
        repo.search(query, callback)

    override fun history(): List<Track> = repo.getHistory()

    override fun pushToHistory(track: Track) = repo.addToHistory(track)

    override fun clearHistory() = repo.clearHistory()
}