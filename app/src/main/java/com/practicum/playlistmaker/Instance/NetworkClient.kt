package com.practicum.playlistmaker.Instance

import com.practicum.playlistmaker.controller.PlaylistApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    val itunesApi: PlaylistApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaylistApi::class.java)
    }
}