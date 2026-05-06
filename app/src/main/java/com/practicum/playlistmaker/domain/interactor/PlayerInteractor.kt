package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.repository.PlayerRepository

interface PlayerInteractor {
    fun prepare(url: String, onPrepared: () -> Unit, onComplete: () -> Unit, onError: (Throwable) -> Unit = {})
    fun play()
    fun pause()
    fun stop()
    fun state(): PlayerState
    fun currentPositionMs(): Int
}

class PlayerInteractorImpl(
    private val repo: PlayerRepository
) : PlayerInteractor {

    override fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) = repo.prepare(url, onPrepared, onComplete, onError)

    override fun play() = repo.play()

    override fun pause() = repo.pause()

    override fun stop() = repo.stop()

    override fun state(): PlayerState = repo.state()

    override fun currentPositionMs(): Int = repo.currentPositionMs()
}