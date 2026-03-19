package com.practicum.playlistmaker.domain.entity

import android.net.Uri

/**
 * Доменная модель плейлиста для слоёв Domain и Presentation.
 */
data class Playlist(
    val id: Long,
    val name: String,
    val description: String,
    /** Uri обложки (в presentation — для Glide и т.д.; файловая работа в data). */
    val coverUri: Uri?,
    val trackIds: List<Long>,
    val trackCount: Int,
)
