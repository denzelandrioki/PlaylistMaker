package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.Track

interface TracksRepository {
    fun search(query: String, callback: (Result<List<Track>>) -> Unit)
    fun getHistory(): List<Track>
    fun addToHistory(track: Track)
    fun clearHistory()
}