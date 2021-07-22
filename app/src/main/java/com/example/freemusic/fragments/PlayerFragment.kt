package com.example.freemusic.fragments

import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.freemusic.R
import com.example.freemusic.databinding.FragmentPlayerBinding
import com.example.freemusic.helper.Constance
import com.example.freemusic.model.FreeMusic
import com.example.freemusic.model.MusicFavorite
import com.example.freemusic.repository.RepositoryRoomDatabaseMusic
import com.example.freemusic.service.PlayerService
import com.example.freemusic.viewmodel.ViewModelRoomDatabaseMusic
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerFragment(private val communication: (Fragment, String) -> Unit) : Fragment() {

    val tagName: String = "PlayerFragment"
    private val binding: FragmentPlayerBinding by lazy {
        FragmentPlayerBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var playerService: PlayerService
    private var isFavorite: Boolean = false
    private var freeMusic: FreeMusic? = null
    private val repoRoomDatabaseMusic: RepositoryRoomDatabaseMusic by lazy {
        RepositoryRoomDatabaseMusic(
            requireActivity().application
        )
    }
    private val viewModelRoomDatabaseMusic: ViewModelRoomDatabaseMusic by lazy {
        ViewModelProvider(
            this,
            ViewModelRoomDatabaseMusic.ViewModelRoomDatabaseMusicProvide(repoRoomDatabaseMusic)
        )[ViewModelRoomDatabaseMusic::class.java]
    }

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), PlayerService::class.java).also {
            requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addEventForWidget()
    }

    private fun addEventForWidget() {
        binding.btnPlay.setOnClickListener {
            playerService.onPauseMusic()
        }
        binding.btnNext.setOnClickListener {
            playerService.onNextMusic()
        }
        binding.btnPrevious.setOnClickListener {
            playerService.onPreviousMusic()
        }
        binding.btnBack.setOnClickListener {
            requireActivity().finish()
        }
        binding.btnFavorite.setOnClickListener {
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24_white)
                isFavorite = false
                CoroutineScope(Dispatchers.IO).launch {
                    val musicFavorite =
                        freeMusic?.let { viewModelRoomDatabaseMusic.checkFavoriteFromTable(it.id) }
                    musicFavorite?.get(0)?.let {
                        viewModelRoomDatabaseMusic.deleteFavorite(it)
                    }
                }
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24_fill)
                isFavorite = true
                freeMusic?.let { music ->
                    if (music.uri != null) {
                        viewModelRoomDatabaseMusic.insertFavorite(
                            MusicFavorite(
                                0,
                                music.id,
                                music.name,
                                music.artists_names,
                                music.duration, music.thumbnail,
                                music.uri.toString()
                            )
                        )
                    } else {
                        viewModelRoomDatabaseMusic.insertFavorite(
                            MusicFavorite(
                                0,
                                music.id,
                                music.name,
                                music.artists_names,
                                music.duration, music.thumbnail,
                                ""
                            )
                        )
                    }
                }
            }
        }
        binding.btnDownload.setOnClickListener {
            downloadMusic()
        }
        binding.btnPlaylists.setOnClickListener {
            val fragment = ListMusicPlayingFragment()
            communication(fragment, fragment.tagName)
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvCurrentPlaying.text = progress.convertMilliSecondToMinute()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playerService.stopPushCurrentPositionMediaPlayer()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    playerService.moveCurrentPositionMediaPlayer(it)
                }
            }

        })
        binding.btnPlayMode.setOnClickListener {
            when (playerService.playMode) {
                Constance.PLAY_MODE_NORMAL -> {
                    playerService.playMode = Constance.PLAY_MODE_REPEAT_ALL
                    binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_24_fill)
                }
                Constance.PLAY_MODE_REPEAT_ALL -> {
                    playerService.playMode = Constance.PLAY_MODE_REPEAT_ONE
                    binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                }
                Constance.PLAY_MODE_REPEAT_ONE -> {
                    playerService.playMode = Constance.PLAY_MODE_NORMAL
                    binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_24)
                }
                else -> {
                    playerService.playMode = Constance.PLAY_MODE_REPEAT_ALL
                    binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_24_fill)
                }
            }
            binding.btnVolume.setImageResource(R.drawable.ic_baseline_shuffle_24)
        }
        binding.btnVolume.setOnClickListener {
            when (playerService.playMode) {
                Constance.PLAY_MODE_SHUFFLE -> {
                    playerService.playMode = Constance.PLAY_MODE_NORMAL
                    binding.btnVolume.setImageResource(R.drawable.ic_baseline_shuffle_24)
                }
                else -> {
                    playerService.playMode = Constance.PLAY_MODE_SHUFFLE
                    binding.btnVolume.setImageResource(R.drawable.ic_baseline_shuffle_24_fill)
                }
            }
            binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_24)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val playerBinder = service as PlayerService.PlayerBinder
            playerService = playerBinder.getPlayerService()
            playerService.freeMusicLiveData.observe(this@PlayerFragment, {
                freeMusic = it
                if (it.uri != null) {
                    binding.tvNameMusic.text = it.name
                    binding.tvArtistMusic.text = it.artists_names
                    binding.tvNameMusic.text = it.name
                    if (getBitmapByUri(
                            requireContext(),
                            it.uri
                        ) != null
                    ) binding.imgAvatarMusic.setImageBitmap(
                        getBitmapByUri(
                            requireContext(),
                            it.uri
                        )
                    )
                    else binding.imgAvatarMusic.setImageResource(R.drawable.ic_launcher_background)

                    binding.btnDownload.isEnabled = false
                    binding.btnFavorite.isEnabled = false
                } else {
                    Picasso.get().load(it.thumbnail.replace("w94", "w320"))
                        .into(binding.imgAvatarMusic)
                    binding.tvNameMusic.text = it.name
                    binding.tvArtistMusic.text = it.artists_names
                    binding.tvNameMusic.text = it.name
                    CoroutineScope(Dispatchers.IO).launch {
                        val arrFavorite = viewModelRoomDatabaseMusic.checkFavoriteFromTable(it.id)
                        if (arrFavorite.size > 0) {
                            withContext(Dispatchers.Main) {
                                binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24_fill)
                            }
                            isFavorite = true
                        } else {
                            withContext(Dispatchers.Main) {
                                binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24_white)
                                isFavorite = false
                            }
                        }
                    }
                }
            })
            when (playerService.playMode) {
                Constance.PLAY_MODE_REPEAT_ONE -> binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                Constance.PLAY_MODE_REPEAT_ALL -> binding.btnPlayMode.setImageResource(R.drawable.ic_baseline_repeat_24_fill)
                Constance.PLAY_MODE_SHUFFLE -> binding.btnVolume.setImageResource(R.drawable.ic_baseline_shuffle_24_fill)
            }
            playerService.playerDurationLiveData.observe(this@PlayerFragment, {
                binding.tvDurationPlaying.text = it.convertMilliSecondToMinute()
                binding.seekBar.max = it
            })
            playerService.isPlayingLiveData.observe(this@PlayerFragment, {
                when (it) {
                    true -> binding.btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
                    false -> binding.btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            })
            playerService.currentPositionMediaPlayerLivaData.observe(this@PlayerFragment, {
                binding.seekBar.progress = it
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    private fun Int.convertMilliSecondToMinute(): String {
        val second = this / 1000
        return when {
            second > 60 && second % 60 > 9 -> "${second / 60}:${second % 60}"
            second > 60 && second % 60 <= 9 -> "${second / 60}:0${second % 60}"
            second < 60 && second % 60 > 9 -> "0:${second % 60}"
            else -> "0:0${second % 60}"
        }
    }

    private fun downloadMusic() {
        if (Constance.isConnectedInternet(requireContext())) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.btnDownload.visibility = View.INVISIBLE
            binding.btnFavorite.isEnabled = false
            val url = "http://api.mp3.zing.vn/api/streaming/audio/${freeMusic?.id}/128"
            val request = DownloadManager.Request(Uri.parse(url))
            val title = URLUtil.guessFileName(url, null, null)
            val cookie = CookieManager.getInstance().getCookie(title)
            request.addRequestHeader("cookie", cookie)
            request.setAllowedOverRoaming(true)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${freeMusic?.name}.mp3"
            )
            val downloadManager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val idDownload = downloadManager.enqueue(request)
            CoroutineScope(Dispatchers.IO).launch {
                var isDownloading = true
                while (isDownloading) {
                    try {
                        val downloadQuery = DownloadManager.Query().setFilterById(idDownload)
                        val cursor: Cursor = downloadManager.query(downloadQuery)
                        cursor.moveToFirst()
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            isDownloading = false
                            withContext(Dispatchers.Main) {
                                binding.progressDownload.visibility = View.INVISIBLE
                                binding.btnDownload.visibility = View.VISIBLE
                                binding.btnFavorite.isEnabled = true
                                Toast.makeText(
                                    requireContext(),
                                    R.string.message_download_done,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("errDownloading", ex.toString())
                    }
                }

            }
        } else Toast.makeText(requireContext(), R.string.message_no_internet, Toast.LENGTH_SHORT)
            .show()
    }

    private fun getBitmapByUri(context: Context, audioUri: Uri): Bitmap? {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unbindService(serviceConnection)
    }

}