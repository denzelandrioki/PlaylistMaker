package com.practicum.playlistmaker.data.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Плейлист с треками (one-to-many) для запросов Room.
 */
data class PlaylistWithTracks(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId",
        entity = PlaylistTrackEntity::class,
    )
    val tracks: List<PlaylistTrackEntity>,
)
