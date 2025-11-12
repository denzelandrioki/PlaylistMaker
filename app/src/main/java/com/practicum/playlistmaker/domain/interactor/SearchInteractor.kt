package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository

class SearchInteractor(private val repo: TracksRepository) {
    fun search(query: String, callback: (Result<List<Track>>) -> Unit) =
        repo.search(query, callback)
    fun history(): List<Track> = repo.getHistory()
    fun pushToHistory(track: Track) = repo.addToHistory(track)
    fun clearHistory() = repo.clearHistory()
}
