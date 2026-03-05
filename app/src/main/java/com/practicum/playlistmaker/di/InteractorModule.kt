package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractorImpl
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor
import com.practicum.playlistmaker.domain.interactor.PlayerInteractorImpl
import com.practicum.playlistmaker.domain.interactor.SearchInteractor
import com.practicum.playlistmaker.domain.interactor.SearchInteractorImpl
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor
import com.practicum.playlistmaker.domain.interactor.SettingsInteractorImpl
import org.koin.dsl.module

/** Интеракторы (use cases): поиск, плеер, настройки, избранное. */
val interactorModule = module {
<<<<<<< Updated upstream
    factory<SearchInteractor>     { SearchInteractorImpl(get()) }
    factory<PlayerInteractor>     { PlayerInteractorImpl(get()) }
    factory<SettingsInteractor>   { SettingsInteractorImpl(get()) }
    factory<FavoritesInteractor>  { FavoritesInteractorImpl(get()) }
=======
    factory<SearchInteractor>    { SearchInteractorImpl(get()) }
    factory<PlayerInteractor>   { PlayerInteractorImpl(get()) }
    factory<SettingsInteractor>  { SettingsInteractorImpl(get()) }
    factory<FavoritesInteractor> { FavoritesInteractorImpl(get()) }
>>>>>>> Stashed changes
}
