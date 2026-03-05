package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.interactor.*
import org.koin.dsl.module

val interactorModule = module {
    factory<SearchInteractor>   { SearchInteractorImpl(get()) }
    factory<PlayerInteractor>   { PlayerInteractorImpl(get()) }
    factory<SettingsInteractor> { SettingsInteractorImpl(get()) }
}
