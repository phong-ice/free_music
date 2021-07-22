package com.example.freemusic.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freemusic.R
import com.example.freemusic.adapter.AdapterListMusicInPlaylist
import com.example.freemusic.databinding.ActivityPlaylistBinding
import com.example.freemusic.helper.Constance
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.repository.RepositoryApiMusic
import com.example.freemusic.repository.RepositoryRoomDatabaseMusic
import com.example.freemusic.service.PlayerService
import com.example.freemusic.viewmodel.ViewModelApiMusic
import com.example.freemusic.viewmodel.ViewModelRoomDatabaseMusic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistFavoriteActivity : AppCompatActivity() {

    private val binding: ActivityPlaylistBinding by lazy {
        ActivityPlaylistBinding.inflate(
            layoutInflater
        )
    }
    private var typePlaylist = ""
    private val repoRoomDatabaseMusic: RepositoryRoomDatabaseMusic by lazy {
        RepositoryRoomDatabaseMusic(
            application
        )
    }
    private val repoApiMusic: RepositoryApiMusic by lazy { RepositoryApiMusic() }
    private val viewModelApiMusic: ViewModelApiMusic by lazy {
        ViewModelProvider(
            this,
            ViewModelApiMusic.ViewModelApiMusicProvide(repoApiMusic)
        )[ViewModelApiMusic::class.java]
    }
    private val viewModelRoomDatabaseMusic: ViewModelRoomDatabaseMusic by lazy {
        ViewModelProvider(
            this,
            ViewModelRoomDatabaseMusic.ViewModelRoomDatabaseMusicProvide(repoRoomDatabaseMusic)
        )[ViewModelRoomDatabaseMusic::class.java]
    }

    private lateinit var adapterRecycler: AdapterListMusicInPlaylist
    private lateinit var listMusic: MutableList<FreeMusic>
    private lateinit var playerService: PlayerService

    override fun onStart() {
        super.onStart()
        Intent(this, PlayerService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        typePlaylist = Constance.ITEM_FAVORITE_PLAYLIST
        initComponent()
        getData()
        handingWidget()
    }

    private fun handingWidget() {
        binding.lvMusicFavorite.apply {
            layoutManager = LinearLayoutManager(this@PlaylistFavoriteActivity)
            adapter = adapterRecycler
        }
        binding.btnBack.setOnClickListener {
            Intent(this, HomeActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    private fun initComponent() {
        listMusic = mutableListOf()
        adapterRecycler = AdapterListMusicInPlaylist(this, listMusic, onItemListClick)
    }

    private fun getData() {
        viewModelRoomDatabaseMusic.getAllFavorite()
        viewModelRoomDatabaseMusic.listFavoriteLiveData.observe(this, { favorites ->
            listMusic.clear()
            CoroutineScope(Dispatchers.IO).launch {
                for ((index, favorite) in favorites.withIndex()) {
                    val freeMusic = if (favorite.uri == "") {
                        FreeMusic(
                            favorite.idMusic,
                            favorite.name,
                            favorite.artists_names,
                            favorite.duration,
                            favorite.thumbnail,
                            null,
                            true
                        )
                    } else {
                        FreeMusic(
                            favorite.idMusic,
                            favorite.name,
                            favorite.artists_names,
                            favorite.duration,
                            favorite.thumbnail,
                            favorite.uri.toUri(),
                            true
                        )
                    }
                    listMusic.add(freeMusic)
                }
                withContext(Dispatchers.Main) {
                    adapterRecycler.notifyDataSetChanged()
                }
            }
        })
    }

    private val onItemListClick: (Int) -> Unit = { i ->
        if (Constance.isConnectedInternet(this)) {
            playerService.pushListMusic(listMusic, i)
            Intent(this, ContentPlayerActivity::class.java).also {
                startActivity(it)
            }
        } else Toast.makeText(this, R.string.message_no_internet, Toast.LENGTH_SHORT).show()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as PlayerService.PlayerBinder
            playerService = iBinder.getPlayerService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

}