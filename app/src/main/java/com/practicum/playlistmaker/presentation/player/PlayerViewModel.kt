package com.practicum.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
import com.practicum.playlistmaker.domain.interactor.PlaylistsInteractor
import com.practicum.playlistmaker.service.PlayerAudioControl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel экрана плеера: избранное и плейлисты через интеракторы;
 * воспроизведение — через [PlayerAudioControl] (bound [com.practicum.playlistmaker.service.PlayerService]).
 */
class PlayerViewModel(
    private val favorites: FavoritesInteractor,
    private val playlists: PlaylistsInteractor,
) : ViewModel() {

    private val _ui = MutableLiveData(PlayerUiState())
    val ui: LiveData<PlayerUiState> = _ui

    private val _playlistsForSheet = MutableLiveData<List<Playlist>>(emptyList())
    val playlistsForSheet: LiveData<List<Playlist>> = _playlistsForSheet

    private val _addToPlaylistResult = MutableLiveData<PlayerAddToPlaylistResult?>(null)
    val addToPlaylistResult: LiveData<PlayerAddToPlaylistResult?> = _addToPlaylistResult

    private var audio: PlayerAudioControl? = null
    private var flowCollectJob: Job? = null

    private var currentTrack: Track? = null

    init {
        playlists.getPlaylists()
            .onEach { _playlistsForSheet.postValue(it) }
            .launchIn(viewModelScope)
    }

    /** После [android.content.ServiceConnection.onServiceConnected]. */
    fun attachPlayer(audioControl: PlayerAudioControl) {
        audio = audioControl
        flowCollectJob?.cancel()
        flowCollectJob = viewModelScope.launch {
            combine(audioControl.playerState, audioControl.progressMs) { state, progress ->
                state to progress
            }.collect { (state, progressMs) ->
                val base = _ui.value ?: return@collect
                _ui.postValue(
                    base.copy(
                        state = state,
                        progressMs = if (state == PlayerState.COMPLETED) 0 else progressMs,
                        canPlay = state != PlayerState.PLAYING && state != PlayerState.PREPARING,
                        canPause = state == PlayerState.PLAYING,
                    ),
                )
            }
        }
        audioControl.setOnPausedByAudioFocusListener {
            val attachedAudioControl = audio ?: return@setOnPausedByAudioFocusListener
            val base = _ui.value ?: return@setOnPausedByAudioFocusListener
            _ui.postValue(
                base.copy(
                    state = PlayerState.PAUSED,
                    progressMs = attachedAudioControl.currentPositionMs(),
                    canPlay = true,
                    canPause = false,
                ),
            )
        }
    }

    fun detachPlayer() {
        flowCollectJob?.cancel()
        flowCollectJob = null
        audio?.setOnPausedByAudioFocusListener(null)
        audio?.hideForegroundNotification()
        audio = null
    }

    /** Экран плеера снова видим — уведомление не нужно. */
    fun onPlayerFragmentStart() {
        audio?.hideForegroundNotification()
    }

    /** Приложение ушло с экрана (в фон) — при активном воспроизведении показываем foreground. */
    fun onPlayerFragmentStop() {
        val audioControl = audio ?: return
        if (audioControl.playerState.value == PlayerState.PLAYING) {
            audioControl.showForegroundNotification()
        }
    }

    fun setTrack(track: Track) {
        currentTrack = track
        viewModelScope.launch {
            val isFav = favorites.isFavorite(track.trackId)
            val base = _ui.value ?: PlayerUiState()
            _ui.postValue(base.copy(track = track, isFavorite = isFav))
        }
    }

    fun preparePlayback(url: String) {
        val audioControl = audio ?: return
        if (url.isBlank()) {
            audioControl.prepare(
                url = "",
                onPrepared = {},
                onComplete = { postCompletionUi() },
                onError = { postCompletionUi() },
            )
            return
        }
        _ui.postValue((_ui.value ?: PlayerUiState()).copy(state = PlayerState.PREPARING, progressMs = 0))
        audioControl.prepare(
            url = url,
            onPrepared = {
                val base = _ui.value ?: return@prepare
                _ui.postValue(base.copy(state = PlayerState.PREPARED, progressMs = 0))
            },
            onComplete = { postCompletionUi() },
            onError = { postCompletionUi() },
        )
    }

    private fun postCompletionUi() {
        val base = _ui.value ?: return
        _ui.postValue(base.copy(state = PlayerState.COMPLETED, progressMs = 0))
    }

    fun playPause() {
        val audioControl = audio ?: return
        when (audioControl.playerState.value) {
            PlayerState.PLAYING -> audioControl.pause()
            PlayerState.PREPARING -> return
            PlayerState.PAUSED, PlayerState.PREPARED, PlayerState.COMPLETED,
            PlayerState.IDLE, PlayerState.ERROR -> audioControl.play()
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        val track = currentTrack ?: return
        if (track.trackId in playlist.trackIds) {
            _addToPlaylistResult.value = PlayerAddToPlaylistResult.AlreadyIn(playlist.name)
            return
        }
        viewModelScope.launch {
            playlists.addTrackToPlaylist(track, playlist)
            _addToPlaylistResult.postValue(PlayerAddToPlaylistResult.Added(playlist.name))
        }
    }

    fun consumeAddToPlaylistResult() {
        _addToPlaylistResult.value = null
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            val base = _ui.value ?: return@launch
            if (base.isFavorite) {
                favorites.removeFromFavorites(trackId = track.trackId)
                _ui.postValue(base.copy(isFavorite = false))
            } else {
                favorites.addToFavorites(track)
                _ui.postValue(base.copy(isFavorite = true))
            }
        }
    }

    override fun onCleared() {
        detachPlayer()
        super.onCleared()
    }
}
