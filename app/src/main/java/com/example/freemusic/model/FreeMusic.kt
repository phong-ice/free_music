package com.example.freemusic.model

import android.net.Uri
import androidx.room.Entity

data class FreeMusic(
    val id:String,
    val name:String,
    val artists_names:String,
    val duration:String,
    val thumbnail:String,
    val uri:Uri?,
    var favorite:Boolean = true
)