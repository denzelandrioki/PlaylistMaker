package com.practicum.playlistmaker.presentation.settings


import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor
class SettingsViewModel(
    private val settings: SettingsInteractor
) : ViewModel() {

    private val _darkTheme = MutableLiveData(settings.isDarkTheme())
    val darkTheme: LiveData<Boolean> = _darkTheme

    fun setDarkTheme(enabled: Boolean) {
        if (_darkTheme.value == enabled) return
        settings.setDarkTheme(enabled)
        _darkTheme.value = enabled

        // применяем сразу (UI-эффект в ответ на состояние VM)
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
