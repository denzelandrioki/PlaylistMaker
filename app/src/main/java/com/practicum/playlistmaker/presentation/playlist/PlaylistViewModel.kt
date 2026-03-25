package com.practicum.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class PlaylistScreenState(
    val playlist: Playlist? = null,
    val durationMinutes: Long = 0L,
    val isLoading: Boolean = true,
)

class PlaylistViewModel(
    private val interactor: PlaylistsInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    private val _state = MutableLiveData<PlaylistScreenState>()
    val state: LiveData<PlaylistScreenState> = _state

    private val _tracks = MutableLiveData<List<Track>>(emptyList())
    val tracks: LiveData<List<Track>> = _tracks

    init {
        loadPlaylist()
        observeTracks()
    }

    private fun loadPlaylist() {
        if (playlistId == 0L) {
            _state.value = PlaylistScreenState(isLoading = false)
            return
        }
        viewModelScope.launch {
            val playlist = interactor.getPlaylistById(playlistId)
            if (playlist == null) {
                _state.value = PlaylistScreenState(isLoading = false)
                return@launch
            }
            _state.value = PlaylistScreenState(
                playlist = playlist,
                durationMinutes = 0L,
                isLoading = false,
            )
        }
    }

    private fun observeTracks() {
        if (playlistId == 0L) return
        interactor.getTracksByPlaylistId(playlistId)
            .onEach { list ->
                _tracks.value = list
                val durationSum = list.sumOf { it.trackTimeMillis }
                val totalMinutes = durationSum / (60 * 1000)
                val playlist = interactor.getPlaylistById(playlistId)
                _state.value = (_state.value ?: PlaylistScreenState()).copy(
                    playlist = playlist,
                    durationMinutes = totalMinutes,
                )
            }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun removeTrack(trackId: Long) {
        viewModelScope.launch {
            interactor.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            interactor.deletePlaylist(playlistId)
            _navigateBackAfterDelete.value = true
        }
    }

    private val _navigateBackAfterDelete = MutableLiveData(false)
    val navigateBackAfterDelete: LiveData<Boolean> = _navigateBackAfterDelete

    fun consumeNavigateBackAfterDelete() {
        _navigateBackAfterDelete.value = false
    }

    fun refreshPlaylist() {
        viewModelScope.launch {
            val playlist = interactor.getPlaylistById(playlistId)
            if (playlist != null) {
                val currentTracks = _tracks.value ?: emptyList()
                val durationSum = currentTracks.sumOf { it.trackTimeMillis }
                val totalMinutes = durationSum / (60 * 1000)
                _state.value = PlaylistScreenState(
                    playlist = playlist,
                    durationMinutes = totalMinutes,
                    isLoading = false,
                )
            }
        }
    }
}
