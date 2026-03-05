package com.practicum.playlistmaker.di

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.data.local.PrefsStorage
import com.practicum.playlistmaker.data.mapper.TrackMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.repository.PrefsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    // Gson
    single { Gson() }

    // SharedPreferences
    single {
        androidContext()
            .getSharedPreferences("playlist_prefs", Context.MODE_PRIVATE)
    }

    // iTunes API (Retrofit service)
    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    // Mapper (object, но регистрируем для явной зависимости)
    single { TrackMapper }

    // Репо настроек (обёртка над SharedPreferences)
    single<PrefsRepository> { PrefsStorage(get()) }
}
