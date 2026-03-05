package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Реализация репозитория треков: поиск через iTunes API, история в SharedPreferences.
 * Для результатов поиска и истории выставляется isFavorite по данным БД избранного.
 */
class TracksRepositoryImpl(
    private val api: ItunesApi,
    private val mapper: TrackMapper,
    private val gson: Gson,
    private val prefs: SharedPreferences,
    private val db: AppDatabase
) : TracksRepository {

    override fun search(query: String): Flow<Result<List<Track>>> = flow {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val response = api.search(query)
                val list = response.results.map { mapper.fromDto(it) }
                val favoriteIds = db.favoriteTracksDao().getFavoriteTrackIds()
                list.forEach { it.isFavorite = it.trackId in favoriteIds }
                list
            }
        }
        emit(result)
    }

    override suspend fun getHistory(): List<Track> = withContext(Dispatchers.IO) {
        val list = readHistory()
        val favoriteIds = db.favoriteTracksDao().getFavoriteTrackIds()
        list.forEach { it.isFavorite = it.trackId in favoriteIds }
        list
    }

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
