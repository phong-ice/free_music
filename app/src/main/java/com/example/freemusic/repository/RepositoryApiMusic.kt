package com.example.freemusic.repository

import com.example.freemusic.helper.RetrofitInstance
import com.example.freemusic.model.ResultDataMusic
import com.example.freemusic.model.ResultRelate
import com.example.freemusic.model.ResultSearch
import com.example.freemusic.model.ResultTopMusic
import retrofit2.Response

class RepositoryApiMusic {
    suspend fun getTopMusic(): Response<ResultTopMusic> = RetrofitInstance.apiMusic.getTopMusic()
    suspend fun getDataMusicByCode(code: String): Response<ResultDataMusic> =
        RetrofitInstance.apiMusic.getDataMusicByCode("audio", code)

    suspend fun searchMusic(patternSearch: String): Response<ResultSearch> =
        RetrofitInstance.apiSearchMusic.searchMusic("song", "100", patternSearch)

    suspend fun getRelateMusic(idMusic: String): Response<ResultRelate> =
        RetrofitInstance.apiMusic.getMusicRelate("audio", idMusic)
}