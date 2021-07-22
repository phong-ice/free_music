package com.example.freemusic.dao

import androidx.room.*
import com.example.freemusic.model.PlayList

@Dao
interface DaoPlaylists {
    @Insert
    suspend fun addPlaylist(playlist: PlayList)

    @Delete
    suspend fun deletePlaylist(playlist: PlayList)

    @Update
    suspend fun updatePlaylist(playlist: PlayList)

    @Query("SELECT * FROM play_lists")
    suspend fun getAllPlaylist(): MutableList<PlayList>

}