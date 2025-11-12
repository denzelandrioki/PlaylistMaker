package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.PlayerState

interface PlayerRepository {
    fun prepare(
        url: String?,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit
    )
    fun play()
    fun pause()
    fun stop()           // <-- добавлено
    fun currentPositionMs(): Int
    fun state(): PlayerState
}