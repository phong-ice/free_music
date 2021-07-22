package com.example.freemusic.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freemusic.adapter.AdapterListPlaying
import com.example.freemusic.databinding.FragmentListMusicPlayingBinding
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.service.PlayerService

class ListMusicPlayingFragment : Fragment() {
    val tagName = "ListMusicPlayingFragment"
    private val binding: FragmentListMusicPlayingBinding by lazy {
        FragmentListMusicPlayingBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var playerService: PlayerService
    private lateinit var listMusic: MutableList<FreeMusic>
    private lateinit var adapterRecyclerView: AdapterListPlaying

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), PlayerService::class.java).also {
            requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponent()
        handingWidget()
    }

    private fun handingWidget() {
        binding.lvListMusic.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterRecyclerView
        }
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun initComponent() {
        listMusic = mutableListOf()
        adapterRecyclerView = AdapterListPlaying(requireContext(), listMusic, onItemClick)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val iBinder = service as PlayerService.PlayerBinder
            playerService = iBinder.getPlayerService()
            playerService.positionPlayingLiveData.observe(this@ListMusicPlayingFragment,{
                adapterRecyclerView.setPositionPlaying(it)
                binding.lvListMusic.scrollToPosition(it)
                adapterRecyclerView.notifyItemChanged(it)
            })
            playerService.listMusicLiveData.observe(this@ListMusicPlayingFragment, {
                listMusic.clear()
                listMusic.addAll(it)
                adapterRecyclerView.notifyDataSetChanged()
            })

        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }

    private val onItemClick: (Int) -> Unit = { position ->
        playerService.changeMusicInListMusic(position)
        adapterRecyclerView.setPositionPlaying(position)
        adapterRecyclerView.notifyItemChanged(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(serviceConnection)
    }

}