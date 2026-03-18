package com.practicum.playlistmaker.presentation.player

/** Результат добавления трека в плейлист для отображения Toast. */
sealed class PlayerAddToPlaylistResult {
    data class Added(val playlistName: String) : PlayerAddToPlaylistResult()
    data class AlreadyIn(val playlistName: String) : PlayerAddToPlaylistResult()
}
