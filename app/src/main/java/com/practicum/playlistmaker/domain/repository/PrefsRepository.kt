package com.practicum.playlistmaker.domain.repository

interface PrefsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}