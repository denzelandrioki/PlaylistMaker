package com.practicum.playlistmaker.data.db

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.domain.entity.Playlist

object PlaylistMapper {

    private val listLongType = object : TypeToken<List<Long>>() {}.type

    fun PlaylistEntity.toPlaylist(gson: Gson): Playlist {
        val ids = gson.fromJson<List<Long>>(trackIdsJson, listLongType) ?: emptyList()
        return Playlist(
            id = id,
            name = name,
            description = description,
            coverPath = coverPath,
            trackIds = ids,
            trackCount = trackCount,
        )
    }

    fun Playlist.toEntity(gson: Gson): PlaylistEntity = PlaylistEntity(
        id = id,
        name = name,
        description = description,
        coverPath = coverPath,
        trackIdsJson = gson.toJson(trackIds),
        trackCount = trackCount,
    )
}
