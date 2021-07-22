package com.example.freemusic.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_favorite")
data class MusicFavorite(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val idMusic:String,
    val name:String,
    val artists_names:String,
    val duration:String,
    val thumbnail:String,
    val uri:String = "",
    val favorite:Boolean = true,
)