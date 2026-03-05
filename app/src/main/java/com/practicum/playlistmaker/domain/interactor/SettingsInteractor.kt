package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.repository.PrefsRepository

/** Слой между UI и PrefsRepository: чтение/запись темы. */
interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}

class SettingsInteractorImpl(
    private val prefs: PrefsRepository
) : SettingsInteractor {

    override fun isDarkTheme(): Boolean = prefs.isDarkTheme()

    override fun setDarkTheme(enabled: Boolean) = prefs.setDarkTheme(enabled)
}