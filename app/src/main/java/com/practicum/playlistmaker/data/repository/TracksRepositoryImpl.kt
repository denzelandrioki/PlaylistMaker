package com.practicum.playlistmaker.data.repository

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.dto.SearchResponseDto
import com.practicum.playlistmaker.data.local.PrefsStorage
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.repository.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(
    private val api: ItunesApi,
    private val prefs: PrefsStorage,
    private val gson: Gson
) : TracksRepository {

    companion object {
        private const val KEY_HISTORY = "history_json"
        private const val HISTORY_LIMIT = 10
        private val MAIN = Handler(Looper.getMainLooper())
        private val LIST_TYPE = object : TypeToken<MutableList<Track>>() {}.type
    }

    override fun search(query: String, callback: (Result<List<Track>>) -> Unit) {
        api.search(query).enqueue(object : Callback<SearchResponseDto> {
            override fun onResponse(call: Call<SearchResponseDto>, resp: Response<SearchResponseDto>) {
                val dtos = resp.body()?.results.orEmpty()
                val tracks = dtos.map(TrackMapper::fromDto)
                MAIN.post { callback(Result.success(tracks)) }
            }
            override fun onFailure(call: Call<SearchResponseDto>, t: Throwable) {
                MAIN.post { callback(Result.failure(t)) }
            }
        })
    }

    override fun getHistory(): List<Track> =
        prefs.getString(KEY_HISTORY)?.let { json ->
            runCatching { gson.fromJson<MutableList<Track>>(json, LIST_TYPE) }.getOrNull() ?: emptyList()
        } ?: emptyList()

    override fun addToHistory(track: Track) {
        val list = getHistory().toMutableList().apply {
            // убираем дубликат по trackId
            removeAll { it.trackId == track.trackId }
            // добавляем в начало
            add(0, track)
            // жёстко ограничиваем размер до HISTORY_LIMIT
            if (size > HISTORY_LIMIT) {
                // оставляем только первые HISTORY_LIMIT элементов
                subList(HISTORY_LIMIT, size).clear()
            }
            // альтернатива без subList:
            // while (size > HISTORY_LIMIT) removeAt(lastIndex)
        }
        prefs.putString(KEY_HISTORY, gson.toJson(list))
    }

    override fun clearHistory() {
        prefs.remove(KEY_HISTORY)
    }
}
