package com.practicum.playlistmaker.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.di.dataModule
import com.practicum.playlistmaker.di.interactorModule
import com.practicum.playlistmaker.di.repositoryModule
import com.practicum.playlistmaker.di.viewModelModule
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Точка входа приложения.
 * Инициализация DI (Koin) и восстановление сохранённой темы через domain-слой.
 * Напрямую с SharedPreferences не работаем — только через SettingsInteractor.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Подключение графа зависимостей: data → repository → interactor → viewModel
        startKoin {
            androidContext(this@App)
            modules(listOf(dataModule, repositoryModule, interactorModule, viewModelModule))
        }

        // Применяем сохранённую тему до отрисовки первого экрана
        val settings: SettingsInteractor = getKoin().get()
        val dark = settings.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
