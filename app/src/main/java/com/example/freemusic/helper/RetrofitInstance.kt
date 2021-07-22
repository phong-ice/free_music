package com.example.freemusic.helper

import com.example.freemusic.dao.MusicApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://mp3.zing.vn/"
    private const val BASE_URL_SEARCH = "http://ac.mp3.zing.vn/"

    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitSearch by lazy {
        Retrofit.Builder().baseUrl(BASE_URL_SEARCH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiMusic: MusicApi by lazy {
        retrofit.create(MusicApi::class.java)
    }

    val apiSearchMusic: MusicApi by lazy {
        retrofitSearch.create(MusicApi::class.java)
    }
}