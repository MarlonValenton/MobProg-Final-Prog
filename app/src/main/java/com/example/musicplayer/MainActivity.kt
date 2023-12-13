package com.example.musicplayer

import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), MediaController.MediaPlayerControl {
    private val songs: ArrayList<Songs> = arrayListOf()
    private val MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1
    private lateinit var recyclerView: RecyclerView
    private var musicService: MusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false
    private lateinit var controller: MusicController
    private var paused: Boolean = false
    private var playbackPaused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_MEDIA_AUDIO)) {
                // Explain to Users Why You Need Permissions
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO)
                }
            }
        } else {
            displaySongs()
        }
        var detailsSong = findViewById<Button>(R.id.action_details)
        detailsSong.setOnClickListener{
            var intent = Intent(this, SongView::class.java)
            startActivity(intent)
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    displaySongs()
                } else {
                    // Handle Denial of Permission
                }
                return
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent!!, musicConnection, BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }
    override fun onPause() {
        super.onPause()
        paused = true
    }
    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }
    override fun onStop() {
        controller.hide()
        super.onStop()
    }
    private fun getSongList() {
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ? AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("%/Music/%", "0")
        val musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, null)
        if ((musicCursor != null) && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisDuration = musicCursor.getString(durationColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                val thisAlbumId = musicCursor.getString(albumIdColumn)
                val albumArtUri = Uri.parse("content://media/external/audio/albumart")
                val albumArtContentUri = ContentUris.withAppendedId(albumArtUri, thisAlbumId.toLong())
                songs.add(Songs(thisId, thisTitle, thisDuration, thisArtist, albumArtContentUri))
            } while (musicCursor.moveToNext())
            musicCursor.close()
        } else {
            Log.d("MyTag", "The song list is empty")
        }
    }
    private fun displaySongs() {
        recyclerView = findViewById(R.id.song_list)
        recyclerView.setHasFixedSize(true)
        getSongList()
        songs.sortWith { a, b -> a.name.compareTo(b.name) }
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        val adapter = RVAdapter(songs)
        recyclerView.adapter = adapter
        setController()
    }
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService
            musicService!!.setList(songs)
            musicBound = true
        }
        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }
    fun songPicked(view: View) {
        musicService!!.setSong(view.tag.toString().toInt())
        musicService!!.playSong()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        controller.show(0)
    }
    private fun playNext() {
        musicService!!.playNext()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        controller.show(0)
    }
    private fun playPrev() {
        musicService!!.playPrev()
        if(playbackPaused){
            setController()
            playbackPaused=false
        }
        controller.show(0)
    }
    private fun setController() {
        controller = MusicController(this)
        controller.setPrevNextListeners({ playNext() }
        ) { playPrev() }
        controller.setMediaPlayer(this)
        controller.setAnchorView(findViewById(R.id.song_list))
        controller.isEnabled = true
    }
    override fun onDestroy() {
        stopService(playIntent)
        musicService = null
        super.onDestroy()
    }
    override fun start() {
        musicService!!.go()
    }
    override fun pause() {
        playbackPaused = true
        musicService!!.pausePlayer()
    }
    override fun seekTo(p0: Int) {
        musicService!!.seek(p0)
    }
    override fun isPlaying(): Boolean {
        if(musicService!=null && musicBound)
            return musicService!!.isPlaying()
        return false
    }
    override fun getDuration(): Int {
        if(musicService!=null && musicBound && musicService!!.isPlaying())
            return musicService!!.getDuration()
        else return 0
    }
    override fun canPause(): Boolean {
        return true
    }
    override fun canSeekBackward(): Boolean {
        return true
    }
    override fun canSeekForward(): Boolean {
        return true
    }
    override fun getAudioSessionId(): Int {
        return 1
    }
    override fun getBufferPercentage(): Int {
        val duration = musicService!!.getDuration()
        if (duration > 0) {
            return (musicService!!.getPosition() * 100)/(duration)
        }
        return 0
    }
    override fun getCurrentPosition(): Int {
        if (musicService != null && musicBound && musicService!!.isPlaying())
            return musicService!!.getPosition()
        else return 0
    }
    fun shuffleSongs(view: View) {
        musicService?.setShuffle()
    }
    fun stopSong(view: View) {
        stopService(playIntent)
        musicService = null
        exitProcess(0)
    }
}
data class Song(val id: Long, val name: String, val duration: String, val artist: String, val cover: Uri)
class RVAdapter(private val songs: ArrayList<Songs>) :
    RecyclerView.Adapter<RVAdapter.SongViewHolder>() {
    class SongViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var songName: TextView = itemView.findViewById(R.id.song_name)
        var songLength: TextView = itemView.findViewById(R.id.song_length)
        var songArtist: TextView = itemView.findViewById(R.id.song_artist)
        var songCover: ImageView = itemView.findViewById(R.id.song_art)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SongViewHolder {
        val v: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.song, viewGroup, false)
        return SongViewHolder(v)
    }

    override fun onBindViewHolder(songViewHolder: SongViewHolder, idx: Int) {
        val duration_minutes_seconds = "${(songs[idx].duration.toInt()/(60*1000)).toString().padStart(2, '0')}:${(songs[idx].duration.toInt()%60).toString().padStart(2, '0')}";
        songViewHolder.songName.text = songs[idx].name
        songViewHolder.songLength.text = duration_minutes_seconds
        songViewHolder.songArtist.text = songs[idx].artist
        songViewHolder.songCover.setImageURI(songs[idx].cover)
        songViewHolder.itemView.tag = idx
    }
}