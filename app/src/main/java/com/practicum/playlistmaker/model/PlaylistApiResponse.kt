package com.practicum.playlistmaker.model

import com.practicum.playlistmaker.dto.TrackDto

data class PlaylistApiResponse(
    val resultCount: Int?,
    val results: List<TrackDto>
)
