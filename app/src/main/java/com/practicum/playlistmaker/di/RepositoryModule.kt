package com.practicum.playlistmaker.di

import android.media.MediaPlayer
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.repository.FavoritesRepositoryImpl
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository
import org.koin.dsl.module

/** Репозитории: TracksRepository, FavoritesRepository (Room), PlayerRepository. */
val repositoryModule = module {

    single<TracksRepository> { TracksRepositoryImpl(api = get(), mapper = get(), gson = get(), prefs = get()) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get<AppDatabase>().favoriteTracksDao()) }

    // Фабрика MediaPlayer
    factory<() -> MediaPlayer> { { MediaPlayer() } }

    // Репозиторий плеера
    single<PlayerRepository> { PlayerRepositoryImpl(mediaPlayerFactory = get()) }
}
