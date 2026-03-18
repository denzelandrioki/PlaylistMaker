package com.practicum.playlistmaker.presentation.player

/** Результат добавления трека в плейлист для отображения Toast. */
sealed interface PlayerAddToPlaylistResult {
    data class Added(val playlistName: String) : PlayerAddToPlaylistResult
    data class AlreadyIn(val playlistName: String) : PlayerAddToPlaylistResult
}
