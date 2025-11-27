package com.practicum.playlistmaker.presentation.player

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor

class PlayerViewModel(
    private val interactor: PlayerInteractor
) : ViewModel() {

    private val _ui = MutableLiveData(PlayerUiState())
    val ui: LiveData<PlayerUiState> = _ui

    private val handler = Handler(Looper.getMainLooper())
    private var ticker: Runnable? = null

    companion object {
        private const val TICK_MS = 333L
    }

    fun init(track: Track) {
        // положим трек в стейт
        _ui.value = _ui.value?.copy(track = track, state = PlayerState.IDLE, progressMs = 0)
        // подготовим плеер
        interactor.prepare(
            url = track.previewUrl.orEmpty(),
            onPrepared = {
                _ui.postValue(_ui.value?.copy(state = PlayerState.PREPARED, canPlay = true, canPause = false))
            },
            onComplete = {
                stopTicker()
                _ui.postValue(_ui.value?.copy(state = PlayerState.COMPLETED, progressMs = 0, canPlay = true, canPause = false))
            },
            onError = {
                stopTicker()
                _ui.postValue(_ui.value?.copy(state = PlayerState.ERROR, progressMs = 0, canPlay = true, canPause = false))
            }
        )
    }

    fun onPlayPauseClicked() {
        when (interactor.state()) {
            PlayerState.PLAYING -> pause()
            PlayerState.PAUSED,
            PlayerState.PREPARED,
            PlayerState.COMPLETED,
            PlayerState.IDLE,
            PlayerState.ERROR -> play()
        }
    }

    private fun play() {
        interactor.play()
        _ui.value = _ui.value?.copy(state = PlayerState.PLAYING, canPlay = false, canPause = true)
        startTicker()
    }

    private fun pause() {
        interactor.pause()
        _ui.value = _ui.value?.copy(state = PlayerState.PAUSED, canPlay = true, canPause = false)
        stopTicker()
    }

    private fun startTicker() {
        stopTicker()
        ticker = object : Runnable {
            override fun run() {
                if (interactor.state() == PlayerState.PLAYING) {
                    _ui.postValue(_ui.value?.copy(progressMs = interactor.currentPositionMs()))
                    handler.postDelayed(this, TICK_MS)
                }
            }
        }.also { handler.post(it) }
    }

    private fun stopTicker() {
        ticker?.let { handler.removeCallbacks(it) }
        ticker = null
    }

    override fun onCleared() {
        stopTicker()
        interactor.stop()
        super.onCleared()
    }
}
