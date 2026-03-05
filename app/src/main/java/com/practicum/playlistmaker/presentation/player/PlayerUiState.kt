package com.practicum.playlistmaker.presentation.player

import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.entity.Track

data class PlayerUiState(
    val track: Track? = null,
    val state: PlayerState = PlayerState.IDLE,
    val progressMs: Int = 0,
    val canPlay: Boolean = true,
    val canPause: Boolean = false,
    val isFavorite: Boolean = false,
)
