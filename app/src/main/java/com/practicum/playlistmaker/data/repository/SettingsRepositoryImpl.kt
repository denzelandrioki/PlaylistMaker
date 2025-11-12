package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.local.PrefsStorage
import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val prefs: PrefsStorage) : SettingsRepository {
    companion object { private const val KEY_DARK = "key_dark_theme" }
    override fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK, false)
    override fun setDarkTheme(enabled: Boolean) = prefs.putBoolean(KEY_DARK, enabled)
}
