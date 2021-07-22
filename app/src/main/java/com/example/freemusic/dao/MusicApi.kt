package com.example.freemusic.dao

import com.example.freemusic.model.ResultDataMusic
import com.example.freemusic.model.ResultRelate
import com.example.freemusic.model.ResultSearch
import com.example.freemusic.model.ResultTopMusic
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApi {

    @GET("xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    suspend fun getTopMusic():Response<ResultTopMusic>

    @GET("/xhr/media/get-source")
    suspend fun getDataMusicByCode(
        @Query("type") type:String,
        @Query("key") code:String
    ):Response<ResultDataMusic>

    @GET("/complete")
    suspend fun searchMusic(
        @Query("type") type: String,
        @Query("num") num: String,
        @Query("query") searchPattern: String,
    ): Response<ResultSearch>

    @GET("xhr/recommend")
    suspend fun getMusicRelate(
        @Query("type") type: String,
        @Query("id") idMusic: String
    ): Response<ResultRelate>
}