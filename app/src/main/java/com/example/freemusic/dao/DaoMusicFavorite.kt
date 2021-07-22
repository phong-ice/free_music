package com.example.freemusic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.freemusic.model.MusicFavorite

@Dao
interface DaoMusicFavorite {

    @Insert
    suspend fun insertMusicFavorite(music: MusicFavorite)

    @Delete
    suspend fun deleteMusicFavorite(music: MusicFavorite)

    @Query("SELECT * FROM music_favorite")
    suspend fun getAllMusicFavorite(): MutableList<MusicFavorite>

    @Query("SELECT * FROM music_favorite WHERE idMusic == :idMusic")
    suspend fun checkFavorite(idMusic: String): MutableList<MusicFavorite>
}