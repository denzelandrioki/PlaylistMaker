package com.practicum.playlistmaker.domain.entity

/**
 * Доменная модель плейлиста для слоёв Domain и Presentation.
 */
data class Playlist(
    val id: Long,
    val name: String,
    val description: String,
    val coverPath: String?,
    val trackIds: List<Long>,
    val trackCount: Int,
)
