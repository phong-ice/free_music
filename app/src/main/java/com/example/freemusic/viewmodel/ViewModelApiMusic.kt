package com.example.freemusic.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.model.MusicSearch
import com.example.freemusic.repository.RepositoryApiMusic
import kotlinx.coroutines.launch

class ViewModelApiMusic(private val repo: RepositoryApiMusic) : ViewModel() {

    val listTopMusicLiveData: MutableLiveData<MutableList<FreeMusic>> = MutableLiveData()
    val listMusicSearched: MutableLiveData<MutableList<MusicSearch>> = MutableLiveData()
    val listMusicRelateLiveData: MutableLiveData<MutableList<FreeMusic>> = MutableLiveData()

    fun getTopMusic() {
        viewModelScope.launch {
            val response = repo.getTopMusic()
            if (response.isSuccessful) {
                listTopMusicLiveData.value = response.body()?.data?.song
            }
        }
    }

    suspend fun getDataMusicByCode(code: String): FreeMusic? {
        var freeMusic: FreeMusic? = null
        val response = repo.getDataMusicByCode(code)
        if (response.isSuccessful) {
            response.body()?.let {
                freeMusic = it.data
            }
        }
        return freeMusic
    }

    suspend fun searchMusicByString(patternSearch: String) {
        val response = repo.searchMusic(patternSearch)
        if (response.isSuccessful) {
            response.body()?.let {
                if (it.data.size > 0) {
                    listMusicSearched.postValue(it.data[0].song)
                }
            }
        }
    }

    fun getMusicRelate(idMusic: String) {
        viewModelScope.launch {
            val response = repo.getRelateMusic(idMusic)
            if (response.isSuccessful) {
                response.body()?.let {
                    listMusicRelateLiveData.value = it.data.items
                }

            }
        }
    }

    class ViewModelApiMusicProvide(private val repo: RepositoryApiMusic) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewModelApiMusic(repo) as T
        }

    }
}