package com.practicum.playlistmaker.domain.interactor

import android.os.Handler
import android.os.Looper
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import java.text.SimpleDateFormat
import java.util.*

class PlayerInteractor(private val repo: PlayerRepository) {

    companion object { private const val PROGRESS_TICK_MS = 333L }

    private val ui = Handler(Looper.getMainLooper())
    private val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
    private var ticker: Runnable? = null

    fun prepare(url: String?, onPrepared: () -> Unit, onCompletion: () -> Unit, onError: (Throwable)->Unit) =
        repo.prepare(url, onPrepared = onPrepared, onCompletion = {
            stopProgress()
            onCompletion()
        }, onError = onError)

    fun play(onProgress: (String) -> Unit) {
        repo.play()
        startProgress(onProgress)
    }

    fun pause() {
        repo.pause()
        stopProgress()
    }

    fun toggle(onProgress: (String) -> Unit) {
        when (repo.state()) {
            com.practicum.playlistmaker.domain.entity.PlayerState.PLAYING -> pause()
            com.practicum.playlistmaker.domain.entity.PlayerState.PAUSED,
            com.practicum.playlistmaker.domain.entity.PlayerState.PREPARED,
            com.practicum.playlistmaker.domain.entity.PlayerState.COMPLETED -> play(onProgress)
            else -> {}
        }
    }

    fun release() { stopProgress(); repo.release() }

    private fun startProgress(onProgress: (String) -> Unit) {
        if (ticker != null) return
        ticker = object : Runnable {
            override fun run() {
                onProgress(sdf.format(repo.currentPositionMs()))
                ui.postDelayed(this, PROGRESS_TICK_MS)
            }
        }.also { ui.post(it) }
    }

    fun stopProgress() {
        ticker?.let { ui.removeCallbacks(it) }
        ticker = null
    }
}
