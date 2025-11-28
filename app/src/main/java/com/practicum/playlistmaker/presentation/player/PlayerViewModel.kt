package com.practicum.playlistmaker.presentation.player

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.interactor.PlayerInteractor

private const val TICK_MS = 333L



class PlayerViewModel(
    private val player: PlayerInteractor
) : ViewModel() {

    private val _ui = MutableLiveData(PlayerUiState())
    val ui: LiveData<PlayerUiState> = _ui

    private var timer: CountDownTimer? = null

    fun prepare(url: String) {
        player.prepare(
            url = url,
            onPrepared = { _ui.postValue(_ui.value!!.copy(state = PlayerState.PREPARED, progressMs = 0)) },
            onComplete = { onCompletion() },
            onError = { onCompletion() }
        )
    }

    fun playPause() {
        when (player.state()) {
            PlayerState.PLAYING -> pause()
            PlayerState.PAUSED, PlayerState.PREPARED, PlayerState.COMPLETED,
            PlayerState.IDLE, PlayerState.ERROR -> play()
        }
    }

    private fun play() {
        player.play()
        _ui.postValue(_ui.value!!.copy(state = PlayerState.PLAYING, canPlay = false, canPause = true))
        startTicker()
    }

    private fun pause() {
        player.pause()
        _ui.postValue(_ui.value!!.copy(state = PlayerState.PAUSED, canPlay = true, canPause = false))
        stopTicker()
    }

    private fun onCompletion() {
        stopTicker()
        _ui.postValue(PlayerUiState(state = PlayerState.COMPLETED, progressMs = 0))
    }

    private fun startTicker() {
        stopTicker()
        timer = object : CountDownTimer(Long.MAX_VALUE, TICK_MS) {
            override fun onTick(millisUntilFinished: Long) {
                if (player.state() == PlayerState.PLAYING) {
                    _ui.postValue(_ui.value!!.copy(progressMs = player.currentPositionMs()))
                }
            }
            override fun onFinish() {}
        }.start()
    }

    private fun stopTicker() {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        stopTicker()
        player.stop()
        super.onCleared()
    }
}