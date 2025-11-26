package com.practicum.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate


/**
 * Application больше НЕ работает с SharedPreferences напрямую.
 * Все чтение/запись темы — через SettingsInteractor (domain->data).
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // читаем текущее состояние темы из домена и применяем
        val isDark = Creator.settingsInteractor(this).isDarkTheme()
        applyTheme(isDark)
    }

    // Применение темы без записи. Сохранением занимается SettingsInteractor.
    fun applyTheme(enable: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enable) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
