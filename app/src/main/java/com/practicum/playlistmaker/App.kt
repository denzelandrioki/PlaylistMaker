package com.practicum.playlistmaker
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_DARK_THEME = "key_dark_theme"
    }

    private lateinit var prefs: SharedPreferences

    var darkTheme: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Если уже сохраняли — берём сохранённое; иначе — текущую системную тему
        darkTheme = if (prefs.contains(KEY_DARK_THEME)) {
            prefs.getBoolean(KEY_DARK_THEME, false)
        } else {
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
        }

        applyTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        if (darkTheme == darkThemeEnabled) return
        darkTheme = darkThemeEnabled
        prefs.edit().putBoolean(KEY_DARK_THEME, darkTheme).apply()
        applyTheme(darkTheme)
    }

    private fun applyTheme(enable: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enable) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}