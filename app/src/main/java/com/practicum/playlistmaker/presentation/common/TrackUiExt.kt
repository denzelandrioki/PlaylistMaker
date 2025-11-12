package com.practicum.playlistmaker.presentation.common

import com.practicum.playlistmaker.domain.entity.Track
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun Track.durationMmSs(): String {
    val sdf = SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format((trackTimeMillis ?: 0L))
}

fun Track.releaseYear(): String? =
    releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
