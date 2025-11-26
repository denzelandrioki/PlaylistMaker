package com.practicum.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val TICK_MS = 333L

data class PlayerUiState(
    val state: PlayerState = PlayerState.IDLE,
    val positionText: String = "00:00"
)

class PlayerViewModel(
    private val interactor: PlayerInteractor
) : ViewModel() {

    private val _ui = MutableLiveData(PlayerUiState())
    val ui: LiveData<PlayerUiState> = _ui

    private var ticker: Job? = null

    fun prepare(url: String) {
        interactor.prepare(
            url = url,
            onPrepared = { _ui.postValue(PlayerUiState(PlayerState.PREPARED, "00:00")) },
            onComplete = { onCompletion() },
            onError = { _ui.postValue(PlayerUiState(PlayerState.ERROR, "00:00")) }
        )
    }

    fun toggle() {
        when (interactor.state()) {
            PlayerState.PLAYING -> pause()
            PlayerState.PAUSED, PlayerState.PREPARED, PlayerState.COMPLETED, PlayerState.IDLE -> play()
            PlayerState.ERROR -> { /* ignore */ }
        }
    }

    private fun play() {
        interactor.play()
        _ui.value = _ui.value?.copy(state = PlayerState.PLAYING)
        startTicker()
    }

    private fun pause() {
        interactor.pause()
        _ui.value = _ui.value?.copy(state = PlayerState.PAUSED)
        stopTicker()
    }

    private fun onCompletion() {
        stopTicker()
        _ui.postValue(PlayerUiState(PlayerState.COMPLETED, "00:00"))
    }

    private fun startTicker() {
        stopTicker()
        ticker = viewModelScope.launch {
            while (isActive && interactor.state() == PlayerState.PLAYING) {
                val ms = interactor.currentPositionMs()
                _ui.postValue(PlayerUiState(PlayerState.PLAYING, formatMs(ms)))
                delay(TICK_MS)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    override fun onCleared() {
        stopTicker()
        interactor.stop()
        super.onCleared()
    }

    private fun formatMs(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(ms)
}
