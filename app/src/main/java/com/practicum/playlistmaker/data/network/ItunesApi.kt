package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API iTunes Search (base URL задаётся в DataModule).
 * Suspend-функция для использования в корутинах и Flow.
 */
interface ItunesApi {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("media") media: String = "music"
    ): SearchResponseDto
}