package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractorImpl
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractorImpl
import com.practicum.playlistmaker.domain.interactor.SearchInteractor
import com.practicum.playlistmaker.domain.interactor.SearchInteractorImpl
import com.practicum.playlistmaker.domain.interactor.SettingsInteractor
import com.practicum.playlistmaker.domain.interactor.SettingsInteractorImpl
import org.koin.dsl.module

/** Интеракторы (use cases): поиск, настройки, избранное, плейлисты. */
val interactorModule = module {
    factory<SearchInteractor>     { SearchInteractorImpl(get()) }
    factory<SettingsInteractor>  { SettingsInteractorImpl(get()) }
    factory<FavoritesInteractor> { FavoritesInteractorImpl(get()) }
    factory<PlaylistsInteractor> { PlaylistsInteractorImpl(get()) }
}
