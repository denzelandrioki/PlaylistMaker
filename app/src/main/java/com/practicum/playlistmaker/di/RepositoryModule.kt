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

<<<<<<< Updated upstream
/** Репозитории: TracksRepository (сеть + история), FavoritesRepository (Room), PlayerRepository (MediaPlayer). */
val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(api = get(), mapper = get(), gson = get(), prefs = get(), db = get())
    }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(dao = get<AppDatabase>().favoriteTracksDao())
    }
=======
/** Репозитории: TracksRepository, FavoritesRepository (Room), PlayerRepository. */
val repositoryModule = module {

    // Репозиторий треков (сеть + gson + prefs + mapper). isFavorite не выставляется здесь — только во ViewModel плеера при входе.
    single<TracksRepository> { TracksRepositoryImpl(api = get(), mapper = get(), gson = get(), prefs = get()) }
>>>>>>> Stashed changes

    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get<AppDatabase>().favoriteTracksDao()) }

    // Фабрика MediaPlayer
    factory<() -> MediaPlayer> { { MediaPlayer() } }

    // Репозиторий плеера
    single<PlayerRepository> { PlayerRepositoryImpl(mediaPlayerFactory = get()) }
}
