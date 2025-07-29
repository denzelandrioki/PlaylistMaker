package com.practicum.playlistmaker.controller

import com.practicum.playlistmaker.model.PlaylistApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaylistApi{


    @GET("/search?entity=song")
    fun searchTracks(@Query("term") term: String): Call<PlaylistApiResponse>

}