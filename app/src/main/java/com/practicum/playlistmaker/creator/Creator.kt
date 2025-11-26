package com.practicum.playlistmaker.creator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.RetrofitProvider
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.interactor.*
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository
import com.practicum.playlistmaker.presentation.player.PlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel

object Creator {

    private const val PREFS_FILE = "playlist_prefs"

    // --- low-level providers
    private fun gson(): Gson = Gson()

    private fun tracksRepository(context: Context): TracksRepository =
        TracksRepositoryImpl(
            api    = RetrofitProvider.itunes(),
            mapper = TrackMapper,
            gson   = gson(),
            prefs  = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        )

    private fun playerRepository(): PlayerRepository = PlayerRepositoryImpl()

    // --- interactors
    private fun searchInteractor(context: Context): SearchInteractor =
        SearchInteractorImpl(tracksRepository(context))

    private fun playerInteractor(): PlayerInteractor =
        PlayerInteractorImpl(playerRepository())

    private fun settingsInteractor(context: Context): SettingsInteractor =
        SettingsInteractorImpl(
            prefs = object : com.practicum.playlistmaker.domain.repository.PrefsRepository {
                private val sp = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                private val KEY = "key_dark_theme"
                override fun isDarkTheme(): Boolean = sp.getBoolean(KEY, false)
                override fun setDarkTheme(enabled: Boolean) { sp.edit().putBoolean(KEY, enabled).apply() }
            }
        )

    // --- ViewModel factories
    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(searchInteractor(context)) as T
            }
        }

    fun providePlayerViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerViewModel(playerInteractor()) as T
            }
        }

    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsInteractor(context)) as T
            }
        }
}
