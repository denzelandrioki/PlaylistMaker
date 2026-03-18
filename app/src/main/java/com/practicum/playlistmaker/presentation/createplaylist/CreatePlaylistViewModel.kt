package com.practicum.playlistmaker.presentation.createplaylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import kotlinx.coroutines.launch

sealed class CreatePlaylistEvent {
    data object NavigateBack : CreatePlaylistEvent()
    data object ShowDiscardDialog : CreatePlaylistEvent()
    /** Показать Toast и затем вернуться назад (после создания плейлиста). */
    data class ShowToastAndNavigate(val playlistName: String) : CreatePlaylistEvent()
}

class CreatePlaylistViewModel(
    private val playlists: PlaylistsInteractor,
) : ViewModel() {

    /** Трек, который нужно добавить в новый плейлист (при переходе из плеера). */
    private var trackToAdd: Track? = null

    private val _title = MutableLiveData("")
    private val _description = MutableLiveData("")
    private val _coverUri = MutableLiveData<String?>(null)
    private val _createButtonEnabled = MutableLiveData(false)
    private val _events = MutableLiveData<CreatePlaylistEvent?>()

    val title: LiveData<String> = _title
    val description: LiveData<String> = _description
    val coverUri: LiveData<String?> = _coverUri
    val createButtonEnabled: LiveData<Boolean> = _createButtonEnabled
    val events: LiveData<CreatePlaylistEvent?> = _events

    fun setTitle(value: String) {
        _title.value = value
        _createButtonEnabled.value = value.trim().isNotBlank()
    }

    fun setDescription(value: String) {
        _description.value = value
    }

    fun setCoverUri(uri: String?) {
        _coverUri.value = uri
    }

    fun setTrackToAdd(track: Track?) {
        trackToAdd = track
    }

    fun hasUnsavedData(): Boolean {
        val t = _title.value?.trim().orEmpty()
        val d = _description.value?.trim().orEmpty()
        val hasCover = _coverUri.value != null
        return t.isNotEmpty() || d.isNotEmpty() || hasCover
    }

    fun onBackPressed() {
        if (hasUnsavedData()) {
            _events.value = CreatePlaylistEvent.ShowDiscardDialog
        } else {
            _events.value = CreatePlaylistEvent.NavigateBack
        }
    }

    fun onDiscardDialogDismissed() {
        _events.value = null
    }

    fun onDiscardConfirm() {
        _events.value = CreatePlaylistEvent.NavigateBack
    }

    fun createPlaylist() {
        val name = _title.value?.trim().orEmpty()
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = playlists.createPlaylist(
                name = name,
                description = _description.value?.trim().orEmpty(),
                coverUri = _coverUri.value,
            )
            trackToAdd?.let { track ->
                playlists.getPlaylistById(id)?.let { playlist ->
                    playlists.addTrackToPlaylist(track, playlist)
                }
            }
            _events.postValue(CreatePlaylistEvent.ShowToastAndNavigate(name))
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
