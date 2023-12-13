package com.example.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var songs: ArrayList<Songs>
    private var songPosition = 0
    private val musicBinder: IBinder = MusicBinder(this)
    private var songTitle: String? = ""
    private val notifyId = 1
    private var shuffle = false
    private var random: Random? = null

    override fun onCreate() {
        super.onCreate()
        songPosition = 0
        mediaPlayer = MediaPlayer()
        initMusicPlayer()
        random = Random.Default
    }

    private fun initMusicPlayer() {
        mediaPlayer.setWakeMode(
            applicationContext,
            PowerManager.PARTIAL_WAKE_LOCK
        )
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
    }

    fun setList(theSongs: ArrayList<Songs>) {
        songs = theSongs
    }

    class MusicBinder(private val service: MusicService) : Binder() {
        val getService: MusicService
            get() = service
    }

    override fun onBind(intent: Intent?): IBinder {
        return musicBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer.stop()
        mediaPlayer.release()
        return false
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        mediaPlayer?.start()
        val channelId = "my_channel_id"
        fun createNotificationChannel(channelId: String, channelName: String): String {
            lateinit var notificationChannel: NotificationChannel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(notificationChannel)
            }
            return channelId
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { intent ->
                PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("Playing")
            .setContentText(songTitle)
            .setContentIntent(pendingIntent)
            .setTicker(songTitle)
            .build()
        createNotificationChannel(channelId, "My Music Player")
        startForeground(notifyId, notification)
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (mediaPlayer.currentPosition > 0) {
            mp.reset()
            playNext()
        }
    }

    fun playSong() {
        mediaPlayer.reset()
        val playSong = songs[songPosition]
        songTitle = playSong.name
        val currentSongId: Long = playSong.id
        val trackUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currentSongId
        )
        try {
            mediaPlayer.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        mediaPlayer.prepare()
    }

    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }

    fun getPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun pausePlayer() {
        mediaPlayer.pause()
    }

    fun seek(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun go() {
        mediaPlayer.start()
    }

    fun playPrev() {
        songPosition--
        if (songPosition < 0) songPosition = songs.size - 1
        playSong()
    }

    fun playNext() {
        if (shuffle) {
            var newSong = songPosition
            while (newSong == songPosition) {
                newSong = random!!.nextInt(songs.size)
            }
            songPosition = newSong
        } else {
            songPosition++
            if (songPosition >= songs.size) songPosition = 0
        }
        playSong()
    }

    fun setShuffle() {
        shuffle = !shuffle
    }
}