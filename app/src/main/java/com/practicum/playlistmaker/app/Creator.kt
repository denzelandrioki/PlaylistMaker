package com.practicum.playlistmaker.app

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.data.local.PrefsStorage
import com.practicum.playlistmaker.data.network.RetrofitProvider
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor
import com.practicum.playlistmaker.domain.interactor.SearchInteractor
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor

object Creator {
    private fun prefs(ctx: Context) =
        PrefsStorage(ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))

    fun searchInteractor(ctx: Context) =
        SearchInteractor(TracksRepositoryImpl(RetrofitProvider.itunes(), prefs(ctx), Gson()))

    fun settingsInteractor(ctx: Context) =
        SettingsInteractor(SettingsRepositoryImpl(prefs(ctx)))

    fun playerInteractor(): PlayerInteractor =
        PlayerInteractor(PlayerRepositoryImpl())
}
