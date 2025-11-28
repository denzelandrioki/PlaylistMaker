package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.repository.TracksRepository
import org.koin.dsl.module

val repositoryModule = module {

    // Репозиторий треков (сеть + gson + prefs + mapper)
    single<TracksRepository> { TracksRepositoryImpl(api = get(), mapper = get(), gson = get(), prefs = get()) }

    // Репозиторий плеера
    single<PlayerRepository> { PlayerRepositoryImpl() }
}
