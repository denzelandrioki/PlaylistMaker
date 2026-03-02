package com.practicum.playlistmaker.domain.repository

/** Настройки приложения (например, тёмная тема), хранятся локально. */
interface PrefsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}