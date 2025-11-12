package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractor(private val repo: SettingsRepository) {
    fun isDark() = repo.isDarkTheme()
    fun setDark(enabled: Boolean) = repo.setDarkTheme(enabled)
}
