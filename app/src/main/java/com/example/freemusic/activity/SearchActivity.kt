package com.example.freemusic.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freemusic.R
import com.example.freemusic.adapter.AdapterListMusicSearch
import com.example.freemusic.databinding.ActivitySearchBinding
import com.example.freemusic.helper.Constance
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.repository.RepositoryApiMusic
import com.example.freemusic.service.PlayerService
import com.example.freemusic.viewmodel.ViewModelApiMusic
import kotlinx.coroutines.*

class SearchActivity : AppCompatActivity() {

    private val binding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(
            layoutInflater
        )
    }
    private val repoApiMusic: RepositoryApiMusic by lazy { RepositoryApiMusic() }
    private val viewModelApiMusic: ViewModelApiMusic by lazy {
        ViewModelProvider(
            this,
            ViewModelApiMusic.ViewModelApiMusicProvide(repoApiMusic)
        )[ViewModelApiMusic::class.java]
    }

    private lateinit var listMusicSearch: MutableList<FreeMusic>
    private lateinit var adapterRecyclerView: AdapterListMusicSearch
    private var coroutineSearch: Job? = null
    private lateinit var playerService: PlayerService
    private var positionItem = 0


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
        getData()
    }

    private fun getData() {
        viewModelApiMusic.listMusicSearched.observe(this, {
            listMusicSearch.clear()
            for (search in it) {
                val freeMusic = FreeMusic(
                    search.id,
                    search.name,
                    search.artist,
                    search.duration,
                    "https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${search.thumb}",
                    null,
                )
                listMusicSearch.add(freeMusic)
            }
            binding.progressLoadListMusic.visibility = View.GONE
            binding.lvMusic.visibility = View.VISIBLE
            adapterRecyclerView.notifyDataSetChanged()
        })
        viewModelApiMusic.listMusicRelateLiveData.observe(this, {
            val listMusicRelate = mutableListOf<FreeMusic>()
            listMusicRelate.add(listMusicSearch[positionItem])
            listMusicRelate.addAll(it)
            playerService.pushListMusic(listMusicRelate, 0)
            Intent(this, ContentPlayerActivity::class.java).also { _intent ->
                startActivity(_intent)
            }
        })
    }

    private fun handingWidget() {
        binding.btnBack.setOnClickListener {
            Intent(this, HomeActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
        binding.lvMusic.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = adapterRecyclerView
        }
        binding.edtSearch.addTextChangedListener { editable ->
            binding.progressLoadListMusic.visibility = View.VISIBLE
            binding.lvMusic.visibility = View.INVISIBLE
            coroutineSearch?.cancel(null)
            coroutineSearch = null
            coroutineSearch = CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                editable?.let {
                    if (it.toString() != "") {
                        if (Constance.isConnectedInternet(this@SearchActivity)) viewModelApiMusic.searchMusicByString(
                            it.toString()
                        ) else withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SearchActivity,
                                R.string.message_no_internet_search,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.progressLoadListMusic.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun initComponent() {
        listMusicSearch = mutableListOf()
        adapterRecyclerView = AdapterListMusicSearch(listMusicSearch, onItemClick)
    }

    private val onItemClick: (Int) -> Unit = {
        positionItem = it
        viewModelApiMusic.getMusicRelate(listMusicSearch[it].id)
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