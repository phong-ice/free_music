package com.example.freemusic.repository

import android.app.Application
import com.example.freemusic.dao.DaoMusicFavorite
import com.example.freemusic.dao.DaoPlaylists
import com.example.freemusic.helper.MusicRoomDatabase
import com.example.freemusic.model.MusicFavorite
import com.example.freemusic.model.PlayList

class RepositoryRoomDatabaseMusic(application: Application) {
    private val daoMusicFavorite: DaoMusicFavorite
    private val daoPlaylists: DaoPlaylists

    init {
        val musicRoomDatabase = MusicRoomDatabase.getInstance(application)
        daoMusicFavorite = musicRoomDatabase.daoMusicFavorite
        daoPlaylists = musicRoomDatabase.daoPlaylists
    }

    //Add music into playlist
    suspend fun insertMusicFavorite(favorite: MusicFavorite) =
        daoMusicFavorite.insertMusicFavorite(favorite)

    suspend fun deleteMusicFavorite(favorite: MusicFavorite) =
        daoMusicFavorite.deleteMusicFavorite(favorite)

    suspend fun getAllMusicFavorite(): MutableList<MusicFavorite> =
        daoMusicFavorite.getAllMusicFavorite()

     suspend  fun checkMusicFavorite(idMusic: String): MutableList<MusicFavorite> =
        daoMusicFavorite.checkFavorite(idMusic)

    //add playlist
    suspend fun insertPlaylist(playList: PlayList) = daoPlaylists.addPlaylist(playList)
    suspend fun updatePlaylist(playList: PlayList) = daoPlaylists.updatePlaylist(playList)
    suspend fun deletePlaylist(playList: PlayList) = daoPlaylists.deletePlaylist(playList)
    suspend fun getAllPlaylists():MutableList<PlayList> = daoPlaylists.getAllPlaylist()
}