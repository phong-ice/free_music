package com.example.freemusic.activity

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freemusic.R
import com.example.freemusic.adapter.AdapterListTopMusic
import com.example.freemusic.databinding.ActivityHomeBinding
import com.example.freemusic.helper.Constance
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.model.MusicFavorite
import com.example.freemusic.repository.RepositoryApiMusic
import com.example.freemusic.repository.RepositoryRoomDatabaseMusic
import com.example.freemusic.service.PlayerService
import com.example.freemusic.viewmodel.ViewModelApiMusic
import com.example.freemusic.viewmodel.ViewModelRoomDatabaseMusic
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val repoApiMusic: RepositoryApiMusic by lazy { RepositoryApiMusic() }
    private val repoRoomDatabaseMusic: RepositoryRoomDatabaseMusic by lazy {
        RepositoryRoomDatabaseMusic(
            application
        )
    }
    private val viewModelApiMusic: ViewModelApiMusic by lazy {
        ViewModelProvider(
            this,
            ViewModelApiMusic.ViewModelApiMusicProvide(repoApiMusic)
        )[ViewModelApiMusic::class.java]
    }
    private val viewModelDatabaseMusic: ViewModelRoomDatabaseMusic by lazy {
        ViewModelProvider(
            this,
            ViewModelRoomDatabaseMusic.ViewModelRoomDatabaseMusicProvide(repoRoomDatabaseMusic)
        )[ViewModelRoomDatabaseMusic::class.java]
    }

    private lateinit var listTopMusic: MutableList<FreeMusic>
    private lateinit var listFavoriteMusic: MutableList<MusicFavorite>
    private lateinit var adapterListMusic: AdapterListTopMusic
    private lateinit var playerService: PlayerService

    override fun onStart() {
        super.onStart()
        Intent(this, PlayerService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onResume() {
        super.onResume()
//        if (Constance.isConnectedInternet(this)) viewModelApiMusic.getTopMusic()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initComponent()
        handingRecyclerView()
        getData()
        addEventWidget()
    }

    private fun addEventWidget() {
        binding.btnPlayMinimize.setOnClickListener {
            playerService.onPauseMusic()
        }
        binding.layoutMinimize.setOnClickListener {
            Intent(this, ContentPlayerActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.layoutPlaylists.playlistFavorite.setOnClickListener {
            Intent(this, PlaylistFavoriteActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.layoutPlaylists.playlistDownload.setOnClickListener {
            Intent(this, PlaylistsDownloadActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.btnSearch.setOnClickListener {
            Intent(this, SearchActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun getData() {
        viewModelApiMusic.listTopMusicLiveData.observe(this, {
            listTopMusic.clear()
            CoroutineScope(Dispatchers.IO).launch {
                for (music in it) {
                    val arrFavorite = viewModelDatabaseMusic.checkFavoriteFromTable(music.id)
                    if (arrFavorite.size > 0) {
                        val freeMusic = FreeMusic(
                            music.id,
                            music.name,
                            music.artists_names,
                            music.duration,
                            music.thumbnail,
                            music.uri,
                            true
                        )
                        listTopMusic.add(freeMusic)
                    } else {
                        val freeMusic = FreeMusic(
                            music.id,
                            music.name,
                            music.artists_names,
                            music.duration,
                            music.thumbnail,
                            music.uri,
                            false
                        )
                        listTopMusic.add(freeMusic)
                    }
                }
                withContext(Dispatchers.Main) {
                    adapterListMusic.notifyDataSetChanged()
                    binding.progressLoadListMusic.visibility = View.GONE
                    binding.lvMusicFavorite.visibility = View.VISIBLE
                }
            }

        })
    }

    private fun handingRecyclerView() {
        binding.lvMusicFavorite.apply {
            adapter = adapterListMusic
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    private fun initComponent() {
        listTopMusic = mutableListOf()
        listFavoriteMusic = mutableListOf()
        adapterListMusic = AdapterListTopMusic(
            listTopMusic,
            onItemClick,
            addMusicToFavoriteList
        )
    }

    private val onItemClick: (Int) -> Unit = { i ->
        if (Constance.isConnectedInternet(this)){
            playerService.pushListMusic(listTopMusic, i)
            Intent(this, ContentPlayerActivity::class.java).also {
                startActivity(it)
            }
        }else Toast.makeText(this, R.string.message_no_internet, Toast.LENGTH_SHORT).show()
    }

    private val addMusicToFavoriteList: (FreeMusic, Int) -> Unit = { music, position ->
        val idMusic = music.id
        CoroutineScope(Dispatchers.IO).launch {
            val arrFavorite = viewModelDatabaseMusic.checkFavoriteFromTable(idMusic)
            if (arrFavorite.size > 0) {
                viewModelDatabaseMusic.deleteFavorite(arrFavorite[0])
            } else viewModelDatabaseMusic.insertFavorite(
                MusicFavorite(
                    0,
                    music.id,
                    music.name,
                    music.artists_names,
                    music.duration,
                    music.thumbnail
                )
            )
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as PlayerService.PlayerBinder
            playerService = iBinder.getPlayerService()
            playerService.freeMusicLiveData.observe(this@HomeActivity, {
                binding.layoutMinimize.visibility = View.VISIBLE
                binding.progressMusicMinimize.visibility = View.VISIBLE
                binding.tvNameMusicMinimize.text = it.name
                if (it.uri != null) {
                    if (getBitmapByUri(
                            this@HomeActivity,
                            it.uri
                        ) != null
                    ) binding.imgAvatarMusic.setImageBitmap(
                        getBitmapByUri(
                            this@HomeActivity,
                            it.uri
                        )
                    ) else binding.imgAvatarMusic.setImageResource(R.drawable.ic_launcher_background)

                } else {
                    Picasso.get().load(it.thumbnail).into(binding.imgAvatarMusic)
                }
            })

            playerService.currentPositionMediaPlayerLivaData.observe(this@HomeActivity, { current ->
                playerService.getDurationMediaPlayer()?.let { _duration ->
                    val progress: Float = (current / _duration.toFloat()) * 100
                    binding.progressMusicMinimize.progress = progress.toInt()
                }

            })
            playerService.isPlayingLiveData.observe(this@HomeActivity, {
                if (it) binding.btnPlayMinimize.setImageResource(R.drawable.ic_baseline_pause_24)
                else binding.btnPlayMinimize.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun getBitmapByUri(context: Context, audioUri: Uri): Bitmap? {
        try {
            val mmr = MediaMetadataRetriever()
            val art: Bitmap
            val bfo = BitmapFactory.Options()
            mmr.setDataSource(context, audioUri)
            val rawArt: ByteArray? = mmr.embeddedPicture
            return if (null != rawArt) {
                art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                art
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)
            }
        } catch (exception: Exception) {
            Log.i("errConvertUriToBitmap", exception.toString())
            return BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_baseline_favorite_24_fill
            )
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (Constance.isConnectedInternet(this@HomeActivity)) {
                false -> binding.tvNoInternet.visibility = View.VISIBLE
                true -> {
                    binding.tvNoInternet.visibility = View.GONE
                    viewModelApiMusic.getTopMusic()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        Intent(this, PlayerService::class.java).also {
            stopService(it)
        }
        unregisterReceiver(broadcastReceiver)
    }
}