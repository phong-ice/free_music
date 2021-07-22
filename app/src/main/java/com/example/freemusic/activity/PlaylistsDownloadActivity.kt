package com.example.freemusic.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freemusic.adapter.AdapterListMusicInPlaylist
import com.example.freemusic.databinding.ActivityPlaylistsDownloadBinding
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.service.PlayerService

class PlaylistsDownloadActivity : AppCompatActivity() {

    private val binding: ActivityPlaylistsDownloadBinding by lazy {
        ActivityPlaylistsDownloadBinding.inflate(layoutInflater)
    }
    private lateinit var playerService: PlayerService
    private lateinit var adapterRecycler: AdapterListMusicInPlaylist
    private lateinit var listMusic: MutableList<FreeMusic>

    override fun onStart() {
        super.onStart()
        Intent(this, PlayerService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initComponent()
        handingWidget()
    }

    private fun handingWidget() {
        binding.lvMusicDownload.apply {
            layoutManager = LinearLayoutManager(this@PlaylistsDownloadActivity)
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
        adapterRecycler = AdapterListMusicInPlaylist(this, listMusic, onItemClick)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as PlayerService.PlayerBinder
            playerService = iBinder.getPlayerService()
            playerService.getAllMusicOffline(application)
            playerService.listMusicExternal.observe(this@PlaylistsDownloadActivity, {
                listMusic.clear()
                listMusic.addAll(it)
                adapterRecycler.notifyDataSetChanged()
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    private val onItemClick: (Int) -> Unit = {
        playerService.pushListMusic(listMusic, it)
        Intent(this, ContentPlayerActivity::class.java).also { _intent ->
            startActivity(_intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}