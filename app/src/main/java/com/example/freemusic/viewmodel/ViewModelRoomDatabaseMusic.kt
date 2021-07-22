package com.example.freemusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freemusic.helper.Constance
import com.example.freemusic.model.MusicFavorite
import com.example.freemusic.model.PlayList
import com.example.freemusic.repository.RepositoryRoomDatabaseMusic
import kotlinx.coroutines.launch

class ViewModelRoomDatabaseMusic(private val repo: RepositoryRoomDatabaseMusic) : ViewModel() {

    val listFavoriteLiveData: MutableLiveData<MutableList<MusicFavorite>> = MutableLiveData()
    val listPlaylistsLiveData: MutableLiveData<MutableList<PlayList>> = MutableLiveData()

    fun insertFavorite(favorite: MusicFavorite) {
        viewModelScope.launch {
            repo.insertMusicFavorite(favorite)
        }
    }

    fun deleteFavorite(favorite: MusicFavorite) {
        viewModelScope.launch {
            repo.deleteMusicFavorite(favorite)
        }
    }

    fun getAllFavorite() {
        viewModelScope.launch {
            listFavoriteLiveData.value = repo.getAllMusicFavorite()
        }
    }

    suspend fun checkFavoriteFromTable(idMusic: String): MutableList<MusicFavorite> = repo.checkMusicFavorite(idMusic)

    //playlist
    fun insertPlaylist(playList: PlayList) {
        viewModelScope.launch {
            repo.insertPlaylist(playList)
        }
    }

    fun updatePlaylist(playList: PlayList) {
        viewModelScope.launch {
            repo.updatePlaylist(playList)
        }
    }

    fun deletePlaylist(playList: PlayList) {
        viewModelScope.launch {
            repo.deletePlaylist(playList)
        }
    }

    fun getAllPlaylists() {
        viewModelScope.launch {
            val listPlayList = repo.getAllPlaylists()
            if (listPlayList.size < 1) {
                repo.insertPlaylist(PlayList(0, Constance.ITEM_FAVORITE_PLAYLIST))
                repo.insertPlaylist(PlayList(0, Constance.ITEM_DOWNLOAD_PLAYLIST))
                repo.insertPlaylist(PlayList(0, Constance.ITEM_ADD_PLAYLIST))
            }
            listPlaylistsLiveData.postValue(repo.getAllPlaylists())
        }
    }

    class ViewModelRoomDatabaseMusicProvide(private val repo: RepositoryRoomDatabaseMusic) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewModelRoomDatabaseMusic(repo) as T
        }
    }
}