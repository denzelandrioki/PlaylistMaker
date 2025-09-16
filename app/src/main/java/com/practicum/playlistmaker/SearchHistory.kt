package com.practicum.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.model.Track

class SearchHistory(private val prefs: SharedPreferences) {


    companion object {
        const val PREFS_NAME = "search_prefs"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }


    private val gson = Gson()
    private val type = object : TypeToken<MutableList<Track>>() {}.type

    fun get(): MutableList<Track> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return mutableListOf()
        return runCatching { gson.fromJson<MutableList<Track>>(json, type) }.getOrElse { mutableListOf() }
    }


    fun notEmpty(): Boolean = prefs.contains(KEY_HISTORY) && get().isNotEmpty()

    fun add(track: Track) {
        val list = get()
        // убираем дубликат по trackId
        list.removeAll { it.trackId == track.trackId }
        // добавляем в начало
        list.add(0, track)
        // ограничиваем до 10
        if (list.size > MAX_SIZE) list.subList(MAX_SIZE, list.size).clear()
        save(list)
    }


    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun save(list: List<Track>) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }




}