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
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Интервал обновления прогресса воспроизведения в UI (мс). */
private const val TICK_MS = 300L

/**
 * ViewModel экрана плеера.
 * isFavorite загружается при входе на экран (setTrack), а не из репозитория поиска.
 */
class PlayerViewModel(
    private val player: PlayerInteractor,
    private val favorites: FavoritesInteractor,
    private val playlists: PlaylistsInteractor,
) : ViewModel() {

    private val _ui = MutableLiveData(PlayerUiState())
    val ui: LiveData<PlayerUiState> = _ui

    private val _playlistsForSheet = MutableLiveData<List<Playlist>>(emptyList())
    val playlistsForSheet: LiveData<List<Playlist>> = _playlistsForSheet

    private val _addToPlaylistResult = MutableLiveData<PlayerAddToPlaylistResult?>(null)
    val addToPlaylistResult: LiveData<PlayerAddToPlaylistResult?> = _addToPlaylistResult

    private var progressJob: Job? = null
    private var currentTrack: Track? = null

    init {
        playlists.getPlaylists()
            .onEach { _playlistsForSheet.postValue(it) }
            .launchIn(viewModelScope)
    }

    /** Вызывается при открытии экрана: сохраняем трек и запрашиваем актуальное isFavorite из БД. */
    fun setTrack(track: Track) {
        currentTrack = track
        viewModelScope.launch {
            val isFav = favorites.isFavorite(track.trackId)
            _ui.postValue(_ui.value!!.copy(track = track, isFavorite = isFav))
        }
    }

    /** Загрузка превью по URL. Вызывать после setTrack(track). */
    fun prepare(url: String) {
        player.prepare(
            url = url,
            onPrepared = { _ui.postValue(_ui.value!!.copy(state = PlayerState.PREPARED, progressMs = 0)) },
            onComplete = { onCompletion() },
            onError = { onCompletion() }
        )
    }

    /** Переключение воспроизведение/пауза в зависимости от текущего state. */
    fun playPause() {
        when (player.state()) {
            PlayerState.PLAYING -> pause()
            PlayerState.PAUSED, PlayerState.PREPARED, PlayerState.COMPLETED,
            PlayerState.IDLE, PlayerState.ERROR -> play()
        }
    }

    private fun play() {
        player.play()
        _ui.value = _ui.value!!.copy(state = PlayerState.PLAYING, canPlay = false, canPause = true)
        startProgressTicker()
    }

    private fun pause() {
        player.pause()
        _ui.value = _ui.value!!.copy(state = PlayerState.PAUSED, canPlay = true, canPause = false)
        stopProgressTicker()
    }

    private fun onCompletion() {
        stopProgressTicker()
        _ui.postValue(_ui.value!!.copy(state = PlayerState.COMPLETED, progressMs = 0))
    }

    /** Добавить текущий трек в выбранный плейлист. Результат — в addToPlaylistResult (Toast). */
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

    /** Добавить/удалить текущий трек из избранного. Состояние обновляется по актуальным данным. */
    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            if (_ui.value!!.isFavorite) {
                favorites.removeFromFavorites(trackId = track.trackId)
                _ui.postValue(_ui.value!!.copy(isFavorite = false))
            } else {
                favorites.addToFavorites(track)
                _ui.postValue(_ui.value!!.copy(isFavorite = true))
            }
        }
    }

    /** Корутина: обновление прогресса раз в 300 мс. Явно сохраняем state = PLAYING, чтобы не перезаписать его старым значением из-за порядка postValue. */
    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = viewModelScope.launch {
            while (isActive && player.state() == PlayerState.PLAYING) {
                _ui.postValue(
                    _ui.value!!.copy(state = PlayerState.PLAYING, progressMs = player.currentPositionMs())
                )
                delay(TICK_MS)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        stopProgressTicker()
        player.stop()
        super.onCleared()
    }
}
