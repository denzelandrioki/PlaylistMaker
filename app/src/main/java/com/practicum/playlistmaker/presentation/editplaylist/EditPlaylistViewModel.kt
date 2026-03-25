package com.practicum.playlistmaker.presentation.editplaylist

import androidx.lifecycle.SavedStateHandle
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistEvent
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistViewModel
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistState
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    playlists: PlaylistsInteractor,
    savedStateHandle: SavedStateHandle,
) : CreatePlaylistViewModel(playlists) {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    init {
        loadPlaylistData()
    }

    /** Загрузить актуальные данные плейлиста из БД (вызывается при каждом открытии экрана). */
    fun loadPlaylistData() {
        if (playlistId != 0L) {
            viewModelScope.launch {
                val playlist = playlists.getPlaylistById(playlistId)
                playlist?.let {
                    val coverUriString = it.coverUri?.toString()
                    // Используем value = вместо postValue(), так как мы уже в корутине
                    _state.value = CreatePlaylistState(
                        title = it.name,
                        description = it.description,
                        coverUri = coverUriString,
                        createButtonEnabled = it.name.isNotBlank(),
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        _events.value = CreatePlaylistEvent.NavigateBack
    }

    override fun createPlaylist() {
        val s = _state.value ?: return
        val name = s.title.trim()
        if (name.isBlank() || playlistId == 0L) return
        viewModelScope.launch {
            playlists.updatePlaylist(
                id = playlistId,
                name = name,
                description = s.description.trim(),
                coverUri = s.coverUri,
            )
            // Небольшая задержка для гарантии, что данные обновились в БД
            kotlinx.coroutines.delay(100)
            _events.postValue(CreatePlaylistEvent.NavigateBack)
        }
    }
}
