package com.practicum.playlistmaker.presentation.createplaylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import com.practicum.playlistmaker.util.SingleLiveEvent
import kotlinx.coroutines.launch

sealed class CreatePlaylistEvent {
    data object NavigateBack : CreatePlaylistEvent()
    data object ShowDiscardDialog : CreatePlaylistEvent()
    /** Показать Toast и затем вернуться назад (после создания плейлиста). */
    data class ShowToastAndNavigate(val playlistName: String) : CreatePlaylistEvent()
}

data class CreatePlaylistState(
    val title: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val createButtonEnabled: Boolean = false,
)

class CreatePlaylistViewModel(
    private val playlists: PlaylistsInteractor,
) : ViewModel() {

    /** Трек, который нужно добавить в новый плейлист (при переходе из плеера). */
    private var trackToAdd: Track? = null

    private val _state = MutableLiveData(CreatePlaylistState())
    val state: LiveData<CreatePlaylistState> = _state

    private val _events = SingleLiveEvent<CreatePlaylistEvent>()
    val events: SingleLiveEvent<CreatePlaylistEvent> = _events

    private fun updateState(block: CreatePlaylistState.() -> CreatePlaylistState) {
        _state.value = (_state.value ?: CreatePlaylistState()).block()
    }

    fun setTitle(value: String) {
        updateState {
            copy(
                title = value,
                createButtonEnabled = value.trim().isNotBlank(),
            )
        }
    }

    fun setDescription(value: String) {
        updateState { copy(description = value) }
    }

    fun setCoverUri(uri: String?) {
        updateState { copy(coverUri = uri) }
    }

    fun setTrackToAdd(track: Track?) {
        trackToAdd = track
    }

    fun hasUnsavedData(): Boolean {
        val s = _state.value ?: return false
        val t = s.title.trim()
        val d = s.description.trim()
        return t.isNotEmpty() || d.isNotEmpty() || s.coverUri != null
    }

    fun onBackPressed() {
        if (hasUnsavedData()) {
            _events.value = CreatePlaylistEvent.ShowDiscardDialog
        } else {
            _events.value = CreatePlaylistEvent.NavigateBack
        }
    }

    fun onDiscardDialogDismissed() {
        // Диалог закрыт без действия — событие уже обработано
    }

    fun onDiscardConfirm() {
        _events.value = CreatePlaylistEvent.NavigateBack
    }

    fun createPlaylist() {
        val s = _state.value ?: return
        val name = s.title.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = playlists.createPlaylist(
                name = name,
                description = s.description.trim(),
                coverUri = s.coverUri,
            )
            trackToAdd?.let { track ->
                playlists.getPlaylistById(id)?.let { playlist ->
                    playlists.addTrackToPlaylist(track, playlist)
                }
            }
            _events.postValue(CreatePlaylistEvent.ShowToastAndNavigate(name))
        }
    }
}
