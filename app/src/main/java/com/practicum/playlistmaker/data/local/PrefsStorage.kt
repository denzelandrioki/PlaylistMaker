package com.practicum.playlistmaker.data.local

import android.content.SharedPreferences
import com.practicum.playlistmaker.domain.repository.PrefsRepository

class PrefsStorage(
    private val prefs: SharedPreferences
) : PrefsRepository {

    companion object {
        private const val KEY_DARK_THEME = "key_dark_theme"
    }

    override fun isDarkTheme(): Boolean =
        prefs.getBoolean(KEY_DARK_THEME, false)

    override fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }
}