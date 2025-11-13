// app/src/main/java/com/practicum/playlistmaker/app/Creator.kt
package com.practicum.playlistmaker.app

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.data.local.PrefsStorage
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.RetrofitProvider
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor
import com.practicum.playlistmaker.domain.interactor.PlayerInteractorImpl
import com.practicum.playlistmaker.domain.interactor.SearchInteractor
import com.practicum.playlistmaker.domain.interactor.SearchInteractorImpl
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor
import com.practicum.playlistmaker.domain.interactor.SettingsInteractorImpl
import com.practicum.playlistmaker.domain.repository.PrefsRepository
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository

object Creator {

    private const val PREFS_FILE = "playlist_prefs"

    // --- low-level providers ---
    private fun gson(): Gson = Gson()

    private fun prefsRepo(context: Context): PrefsRepository =
        PrefsStorage(context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE))

    private fun tracksRepository(context: Context): TracksRepository =
        TracksRepositoryImpl(
            api   = RetrofitProvider.itunes(),
            mapper= TrackMapper,
            gson  = gson(),
            prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        )

    private fun playerRepository(): PlayerRepository = PlayerRepositoryImpl()

    // --- interactors ---
    fun searchInteractor(context: Context): SearchInteractor =
        SearchInteractorImpl(tracksRepository(context))

    fun settingsInteractor(context: Context): SettingsInteractor =
        SettingsInteractorImpl(prefsRepo(context))

    fun playerInteractor(): PlayerInteractor =
        PlayerInteractorImpl(playerRepository())
}
