package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.SearchResponseDto
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(
    private val api: ItunesApi,
    private val mapper: TrackMapper,
    private val gson: Gson,
    private val prefs: SharedPreferences
) : TracksRepository {

    override fun search(query: String, callback: (Result<List<Track>>) -> Unit) {
        api.search(query).enqueue(object : Callback<SearchResponseDto> {
            override fun onResponse(
                call: Call<SearchResponseDto>,
                response: Response<SearchResponseDto>
            ) {
                if (!response.isSuccessful) {
                    callback(Result.failure(IllegalStateException("HTTP ${response.code()}")))
                    return
                }
                val body = response.body()
                val list = body?.results.orEmpty().map { mapper.fromDto(it) }
                callback(Result.success(list))
            }

            override fun onFailure(call: Call<SearchResponseDto>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    override fun getHistory(): List<Track> = readHistory()

    override fun addToHistory(track: Track) {
        val list = readHistory().toMutableList()
        // убрать дубликаты по trackId
        list.removeAll { it.trackId == track.trackId }
        // ограничить размер (до 10): если переполнено — убрать последний элемент
        if (list.size >= MAX_HISTORY && list.isNotEmpty()) {
            list.removeAt(list.lastIndex) // <-- вместо removeLast() (API 35)
        }
        // добавить в начало
        list.add(0, track)
        writeHistory(list)
    }

    override fun clearHistory() = writeHistory(emptyList())

    // --- приватные помощники ---
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