package com.practicum.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Интервал обновления прогресса воспроизведения в UI (мс). */
private const val TICK_MS = 300L

/**
 * ViewModel экрана плеера.
 * Прогресс обновляется корутиной раз в 300 мс; избранное через FavoritesInteractor.
 */
class PlayerViewModel(
    private val player: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _ui = MutableLiveData(PlayerUiState())
    val ui: LiveData<PlayerUiState> = _ui

    private var progressJob: Job? = null

    /** Установить текущий трек (вызывается при открытии экрана). */
    fun setTrack(track: Track) {
        _ui.value = _ui.value!!.copy(track = track, isFavorite = track.isFavorite)
    }

    /** Вызывается при открытии экрана: загрузка превью по URL, коллбэки — на главном потоке. */
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
        _ui.postValue(PlayerUiState(state = PlayerState.COMPLETED, progressMs = 0))
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

    /** Нажатие на кнопку «Нравится»: добавить в избранное или удалить. */
    fun onFavoriteClicked() {
        val current = _ui.value ?: return
        val track = current.track ?: return
        viewModelScope.launch {
            if (current.isFavorite) {
                favoritesInteractor.removeFromFavorites(track)
                _ui.postValue(current.copy(isFavorite = false))
            } else {
                favoritesInteractor.addToFavorites(track)
                _ui.postValue(current.copy(isFavorite = true))
            }
        }
    }

    override fun onCleared() {
        stopProgressTicker()
        player.stop()
        super.onCleared()
    }
}
