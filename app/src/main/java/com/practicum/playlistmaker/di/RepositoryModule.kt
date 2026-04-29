package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.repository.FavoritesRepositoryImpl
import com.practicum.playlistmaker.data.repository.PlaylistsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import com.practicum.playlistmaker.domain.repository.PlaylistsRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Репозитории: TracksRepository, FavoritesRepository (Room), PlaylistsRepository. */
val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(
            appContext = androidContext().applicationContext,
            api = get(),
            mapper = get(),
            gson = get(),
            prefs = get(),
        )
    }
    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get<AppDatabase>().favoriteTracksDao()) }
    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            playlistsDao = get<AppDatabase>().playlistsDao(),
            playlistTracksDao = get<AppDatabase>().playlistTracksDao(),
            context = androidContext(),
        )
    }

}
