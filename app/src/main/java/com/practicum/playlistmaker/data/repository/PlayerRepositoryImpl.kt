package com.practicum.playlistmaker.data.repository


import android.media.MediaPlayer
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.repository.PlayerRepository

class PlayerRepositoryImpl(
    private val mediaPlayerFactory: () -> MediaPlayer
) : PlayerRepository {

    private var mp: MediaPlayer? = null
    private var state: PlayerState = PlayerState.IDLE

    override fun prepare(
        url: String?,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        stop()
        if (url.isNullOrBlank()) {
            onError(IllegalArgumentException("previewUrl is null"))
            return
        }
        mp = mediaPlayerFactory().apply {
            try {
                setDataSource(url)
                setOnPreparedListener {
                    state = PlayerState.PREPARED
                    onPrepared()
                }
                setOnCompletionListener {
                    state = PlayerState.COMPLETED
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    state = PlayerState.ERROR
                    onError(IllegalStateException("MediaPlayer error"))
                    true
                }
                prepareAsync()
            } catch (t: Throwable) {
                state = PlayerState.ERROR
                onError(t)
            }
        }
    }

    override fun play() {
        mp?.start()
        state = PlayerState.PLAYING
    }

    override fun pause() {
        mp?.pause()
        state = PlayerState.PAUSED
    }

    override fun stop() {
        mp?.release()
        mp = null
        state = PlayerState.IDLE
    }

    override fun currentPositionMs(): Int = mp?.currentPosition ?: 0

    override fun state(): PlayerState = state
}