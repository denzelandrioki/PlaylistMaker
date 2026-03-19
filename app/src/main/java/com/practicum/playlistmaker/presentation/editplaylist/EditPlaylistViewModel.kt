package com.practicum.playlistmaker.presentation.editplaylist

import androidx.lifecycle.SavedStateHandle
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistEvent
import com.practicum.playlistmaker.presentation.createplaylist.CreatePlaylistViewModel
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    playlists: PlaylistsInteractor,
    savedStateHandle: SavedStateHandle,
) : CreatePlaylistViewModel(playlists) {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    init {
        if (playlistId != 0L) {
            viewModelScope.launch {
                playlists.getPlaylistById(playlistId)?.let { playlist ->
                    setInitialState(
                        title = playlist.name,
                        description = playlist.description,
                        coverUri = playlist.coverUri?.toString(),
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
            _events.postValue(CreatePlaylistEvent.NavigateBack)
        }
    }
}
