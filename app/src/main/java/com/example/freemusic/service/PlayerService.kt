package com.example.freemusic.service

import android.app.Application
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.example.freemusic.R
import com.example.freemusic.activity.HomeActivity
import com.example.freemusic.helper.Constance
import com.example.freemusic.helper.MyApplication.Companion.CHANNEL_ID
import com.example.freemusic.model.FreeMusic
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.*
import kotlin.random.Random

class PlayerService : Service() {

    inner class PlayerBinder : Binder() {
        fun getPlayerService(): PlayerService {
            return this@PlayerService
        }
    }

    private val iBinder = PlayerBinder()

    //liveData
    val listMusicLiveData: MutableLiveData<MutableList<FreeMusic>> = MutableLiveData()
    val freeMusicLiveData: MutableLiveData<FreeMusic> = MutableLiveData()
    val isPlayingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val currentPositionMediaPlayerLivaData: MutableLiveData<Int> = MutableLiveData()
    val listMusicExternal: MutableLiveData<MutableList<FreeMusic>> = MutableLiveData()
    val playerDurationLiveData: MutableLiveData<Int> = MutableLiveData()
    val positionPlayingLiveData: MutableLiveData<Int> = MutableLiveData()

    //other
    private val listMusic: MutableList<FreeMusic> = mutableListOf()
    private var freeMusic: FreeMusic? = null
    var position: Int = -1
    private var mediaPlayer: MediaPlayer? = null
    private var coroutineListenerChangeCurrentMediaPlayer: Job? = null
    var playMode: String = Constance.PLAY_MODE_NORMAL
    private var drawablePlay = R.drawable.ic_baseline_pause_24

    //control music by notification
    private val pendingIntentNext: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            this,
            1,
            Intent(Constance.ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private val pendingIntentPlay: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            this,
            2,
            Intent(Constance.ACTION_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private val pendingIntentPrevious: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            this,
            3,
            Intent(Constance.ACTION_PREVIOUS),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(broadcastReceiver, IntentFilter(Constance.ACTION_PREVIOUS))
        registerReceiver(broadcastReceiver, IntentFilter(Constance.ACTION_PLAY))
        registerReceiver(broadcastReceiver, IntentFilter(Constance.ACTION_NEXT))
//        registerReceiver(broadcastReceiver, IntentFilter(Constance.ACTION_STOP_SERVICE))
    }

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        getAllMusicOffline(application)
        return START_STICKY
    }

    fun pushListMusic(_listMusic: MutableList<FreeMusic>, _position: Int) {
        listMusic.clear()
        listMusic.addAll(_listMusic)
        freeMusicLiveData.value = _listMusic[_position]
        listMusicLiveData.value = _listMusic
        position = _position
        onPlayMusic()
    }

    private fun onPlayMusic() {
        freeMusic = listMusic[position]
        freeMusic?.let { freeMusicLiveData.value = it }
        mediaPlayer?.stop()
        freeMusic?.let {
            mediaPlayer = if (it.uri != null) {
                MediaPlayer.create(
                    this,
                    it.uri
                )
            } else {
                MediaPlayer.create(
                    this,
                    Uri.parse("http://api.mp3.zing.vn/api/streaming/audio/${it.id}/128")
                )
            }
        }
        mediaPlayer?.start()
        createNotification()
        positionPlayingLiveData.value = position
        drawablePlay = R.drawable.ic_baseline_pause_24
        playerDurationLiveData.value = mediaPlayer?.duration
        listenerChangeCurrentPositionMediaPlayer()
        isPlayingLiveData.value = mediaPlayer?.isPlaying
    }

    fun changeMusicInListMusic(pos: Int) {
        if (freeMusic?.uri == null) {
            if (Constance.isConnectedInternet(this)) {
                position = pos
                onPlayMusic()
            } else Toast.makeText(this, R.string.message_no_internet, Toast.LENGTH_SHORT).show()
        } else {
            position = pos
            onPlayMusic()
        }
    }

    fun onPauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlayingLiveData.value = mediaPlayer?.isPlaying
            drawablePlay = R.drawable.ic_baseline_play_arrow_24
        } else {
            mediaPlayer?.start()
            isPlayingLiveData.value = mediaPlayer?.isPlaying
            drawablePlay = R.drawable.ic_baseline_pause_24
        }
        createNotification()
    }

    fun onNextMusic() {
        if (freeMusic?.uri == null) {
            if (Constance.isConnectedInternet(this)) {
                if (position < listMusic.size - 1) {
                    position++
                    freeMusic = listMusic[position]
                    onPlayMusic()
                }
            } else Toast.makeText(this, R.string.message_no_internet, Toast.LENGTH_SHORT).show()
        } else {
            if (position < listMusic.size - 1) {
                position++
                freeMusic = listMusic[position]
                onPlayMusic()
            }
        }
    }

    fun onStopMusic() {
        stopForeground(true)
        coroutineListenerChangeCurrentMediaPlayer?.cancel(null)
        coroutineListenerChangeCurrentMediaPlayer = null
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    fun onPreviousMusic() {
        if (freeMusic?.uri == null) {
            if (Constance.isConnectedInternet(this)) {
                if (position > 0) {
                    position--
                    freeMusic = listMusic[position]
                    onPlayMusic()
                }
            } else Toast.makeText(this, R.string.message_no_internet, Toast.LENGTH_SHORT).show()
        } else {
            if (position > 0) {
                position--
                freeMusic = listMusic[position]
                onPlayMusic()
            }
        }
    }

    fun getDurationMediaPlayer(): Int? = mediaPlayer?.duration

    fun moveCurrentPositionMediaPlayer(progress: Int) {
        mediaPlayer?.seekTo(progress)
        listenerChangeCurrentPositionMediaPlayer()
    }

    fun stopPushCurrentPositionMediaPlayer() =
        coroutineListenerChangeCurrentMediaPlayer?.cancel(null)

    private fun listenerChangeCurrentPositionMediaPlayer() {
        coroutineListenerChangeCurrentMediaPlayer?.cancel(null)
        coroutineListenerChangeCurrentMediaPlayer = null
        coroutineListenerChangeCurrentMediaPlayer = CoroutineScope(Dispatchers.IO).launch {
            mediaPlayer?.duration?.let {
                repeat(it) {
                    withContext(Dispatchers.Main) {
                        currentPositionMediaPlayerLivaData.value = mediaPlayer?.currentPosition
                    }
                    mediaPlayer?.let { mediaPlayer ->
                        if ((mediaPlayer.currentPosition + 100) > mediaPlayer.duration) {
                            when (playMode) {
                                Constance.PLAY_MODE_NORMAL -> {
                                    withContext(Dispatchers.Main) {
                                        onNextMusic()
                                    }
                                }
                                Constance.PLAY_MODE_REPEAT_ONE -> withContext(Dispatchers.Main) {
                                    onPlayMusic()
                                }
                                Constance.PLAY_MODE_REPEAT_ALL -> {
                                    if (position == listMusic.size - 1) position = -1
                                    withContext(Dispatchers.Main) {
                                        onNextMusic()
                                    }
                                }
                                Constance.PLAY_MODE_SHUFFLE -> {
                                    position = Random.nextInt(0, listMusic.size)
                                    withContext(Dispatchers.Main) {
                                        onPlayMusic()
                                    }
                                }
                            }

                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    fun getAllMusicOffline(application: Application) {
        val listMusic: MutableList<FreeMusic> = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            val collection = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
                else -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            )
            application.applicationContext.contentResolver.query(
                collection,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val name = cursor.getString(1)
                    val artists = cursor.getString(2)
                    val duration = cursor.getInt(3)
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val iceMusic =
                        FreeMusic(id.toString(), name, artists, duration.toString(), "", uri)
                    listMusic.add(iceMusic)
                }
                withContext(Dispatchers.Main) {
                    listMusicExternal.value = listMusic
                }
            }
        }
    }

    private fun createNotification() {
        var mBitmap: Bitmap? = null
        if (listMusic[position].uri != null) {
            listMusic[position].uri?.let {
                mBitmap = if (getBitmapByUri(this, it) != null) getBitmapByUri(this, it)
                else BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_baseline_favorite_24_fill
                )
            }
        } else {
            Picasso.get().load(listMusic[position].thumbnail).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    mBitmap = bitmap
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_favorite_24)
            .setContentTitle(listMusic[position].name)
            .setContentText(listMusic[position].artists_names)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    101,
                    Intent(this, HomeActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(R.drawable.ic_baseline_fast_rewind_24, "Previous", pendingIntentPrevious)
            .addAction(drawablePlay, "Play or pause", pendingIntentPlay)
            .addAction(R.drawable.ic_baseline_fast_forward_24, "Next", pendingIntentNext)
//            .addAction(
//                R.drawable.ic_baseline_close_24, "close", PendingIntent.getBroadcast(
//                    this, 102,
//                    Intent(Constance.ACTION_STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT
//                )
//            )
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setLargeIcon(mBitmap)
            .build()
        NotificationManagerCompat.from(this).notify(1, notification)
        startForeground(1, notification)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constance.ACTION_NEXT -> onNextMusic()
                Constance.ACTION_PLAY -> onPauseMusic()
                Constance.ACTION_PREVIOUS -> onPreviousMusic()
                Constance.ACTION_STOP_SERVICE -> onStopMusic()
            }
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
            return BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_baseline_favorite_24_fill
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        Log.i("test123", "onDestroy Service")
        stopForeground(true)
        coroutineListenerChangeCurrentMediaPlayer?.cancel(null)
        coroutineListenerChangeCurrentMediaPlayer = null
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}