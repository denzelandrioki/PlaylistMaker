package com.practicum.playlistmaker.di

import androidx.lifecycle.SavedStateHandle
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistViewModel
import com.practicum.playlistmaker.presentation.editplaylist.EditPlaylistViewModel
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.playlist.PlaylistViewModel
import com.practicum.playlistmaker.presentation.player.PlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/** Регистрация ViewModel для Koin (по одному на экран/логический блок). */
val viewModelModule = module {
    viewModel { SearchViewModel(get()) }
    viewModel { PlayerViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
    viewModel { CreatePlaylistViewModel(get()) }
    viewModel { (state: SavedStateHandle) -> EditPlaylistViewModel(get(), state) }
    viewModel { (state: SavedStateHandle) -> PlaylistViewModel(get(), state) }
}
