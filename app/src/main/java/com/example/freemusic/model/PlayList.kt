package com.example.freemusic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_lists")
data class PlayList(
    @PrimaryKey(autoGenerate = true)
    val idPlayList:Int,
    val namePlaylist:String
)