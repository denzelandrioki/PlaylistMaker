package com.practicum.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor

class SettingsViewModel(
    private val settings: SettingsInteractor
) : ViewModel() {

    private val _dark = MutableLiveData<Boolean>(settings.isDarkTheme())
    val dark: LiveData<Boolean> = _dark

    fun toggle(value: Boolean) {
        if (_dark.value == value) return
        _dark.value = value
        settings.setDarkTheme(value)
    }
}
