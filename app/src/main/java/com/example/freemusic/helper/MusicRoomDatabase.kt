package com.example.freemusic.helper

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.freemusic.dao.DaoMusicFavorite
import com.example.freemusic.dao.DaoPlaylists
import com.example.freemusic.model.MusicFavorite
import com.example.freemusic.model.PlayList

@Database(entities = [MusicFavorite::class,PlayList::class], version = 5, exportSchema = false)
abstract class MusicRoomDatabase : RoomDatabase() {

    abstract val daoMusicFavorite:DaoMusicFavorite
    abstract val daoPlaylists:DaoPlaylists

    companion object {
        private const val MUSIC_ROOM_DATABASE = "music_room_database"
        private var INSTANCE: MusicRoomDatabase? = null
        fun getInstance(context: Context): MusicRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, MusicRoomDatabase::class.java,
                        MUSIC_ROOM_DATABASE
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }
}