package com.practicum.playlistmaker.data.local

import android.content.SharedPreferences

class PrefsStorage(private val prefs: SharedPreferences) {
    fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun getString(key: String): String? = prefs.getString(key, null)
    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, def: Boolean = false): Boolean = prefs.getBoolean(key, def)
    fun remove(key: String) = prefs.edit().remove(key).apply()
}
