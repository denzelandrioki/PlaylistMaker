package com.practicum.playlistmaker.service

import com.practicum.playlistmaker.domain.entity.PlayerState
import kotlinx.coroutines.flow.StateFlow

/**
 * Публичный контракт аудиоплеера в [PlayerService]: воспроизведение, состояние и foreground-уведомление.
 */
interface PlayerAudioControl {
    val playerState: StateFlow<PlayerState>
    val progressMs: StateFlow<Int>

    fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit,
    )

    fun play()
    fun pause()
    fun stop()

    fun currentPositionMs(): Int

    /** Если воспроизведение приостановлено из‑за потери аудиофокуса (звонок и т.п.). */
    fun setOnPausedByAudioFocusListener(listener: (() -> Unit)?)

    /** Показать foreground-уведомление (при свёрнутом приложении во время воспроизведения). */
    fun showForegroundNotification()

    /** Убрать уведомление (при возврате на экран или при остановке/завершении). */
    fun hideForegroundNotification()
}
