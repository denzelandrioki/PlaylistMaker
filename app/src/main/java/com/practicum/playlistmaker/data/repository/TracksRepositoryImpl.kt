package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Реализация репозитория треков: поиск через iTunes API (suspend) и локальная история в SharedPreferences.
 * search() возвращает Flow с одним Result.
 */
class TracksRepositoryImpl(
    private val api: ItunesApi,
    private val mapper: TrackMapper,
    private val gson: Gson,
    private val prefs: SharedPreferences
) : TracksRepository {

    override fun search(query: String): Flow<Result<List<Track>>> = flow {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val response = api.search(query)
                response.results.map { mapper.fromDto(it) }
            }
        }
        emit(result)
    }

    override fun getHistory(): List<Track> = readHistory()

    override fun addToHistory(track: Track) {
        val list = readHistory().toMutableList()
        list.removeAll { it.trackId == track.trackId }
        if (list.size >= MAX_HISTORY && list.isNotEmpty()) {
            list.removeAt(list.lastIndex)
        }
        list.add(0, track)
        writeHistory(list)
    }

    override fun clearHistory() = writeHistory(emptyList())

    private fun readHistory(): List<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<Track>>(json, typeTrackList) }
            .getOrElse { emptyList() }
    }

    private fun writeHistory(list: List<Track>) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list, typeTrackList)).apply()
    }

    companion object {
        private const val KEY_HISTORY = "search_history"
        private const val MAX_HISTORY = 10
        private val typeTrackList = object : TypeToken<List<Track>>() {}.type
    }
}
