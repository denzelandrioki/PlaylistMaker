package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.SearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    fun search(
        @Query("term") term: String,
        @Query("media") media: String = "music"
    ): Call<SearchResponseDto>
}
