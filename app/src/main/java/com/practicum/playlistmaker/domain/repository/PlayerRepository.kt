package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.entity.PlayerState

/** Контракт воспроизведения: подготовка по URL, play/pause/stop, позиция и состояние. */
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

    /** Вызывается, если воспроизведение приостановлено из‑за потери аудиофокуса (звонок и т.п.). */
    fun setOnPausedByAudioFocusListener(listener: (() -> Unit)?)
}