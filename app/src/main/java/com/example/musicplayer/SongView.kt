package com.example.musicplayer

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.io.IOException

data class  Songinformation (var name: String, var artist: String, var photoAlbum: Int) {

}

class SongView : AppCompatActivity(){

    private lateinit var mp3: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var timeBar: TextView
    private lateinit var songTitleTextView: TextView
    private lateinit var songArtistTextView: TextView
    private lateinit var songAlbum: ImageView
    private lateinit var song: ArrayList<Int>
    private lateinit var songList: ArrayList<Songinformation>

    private var currentSongIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.songview)

        //song details in array form
        song = ArrayList()
        //song.add("/storage/emulated/0/Audio/Perfect Night.mp3")
        song.add(R.raw.perfect_night)
        song.add(R.raw.asap)
        songTitleTextView = findViewById(R.id.txtMusicTitle)
        songArtistTextView = findViewById(R.id.txtMusicArtist)
        songAlbum = findViewById(R.id.imgAlbum)
        songList = ArrayList()
        songList.add(Songinformation("Perfect Night", "Le Sserafim", R.drawable.perfect_night))
        songList.add(Songinformation("Asap", "New Jeans", R.drawable.asap))

        timeBar = findViewById(R.id.txtTime)
        seekBar = findViewById(R.id.MusicBar)
        mp3 = MediaPlayer.create(this, song[currentSongIndex])


        var play = findViewById<ImageView>(R.id.btnPlay)
        var pause = findViewById<ImageView>(R.id.btnPause)
        var previousMusic = findViewById<ImageView>(R.id.btnBackMusic)
        var nextMusic = findViewById<ImageView>(R.id.btnNextMusic)

        var editSong = findViewById<ImageView>(R.id.edtSong)
        editSong.setOnClickListener{
            var intent = Intent(this, EditSongActivity::class.java)
            startActivity(intent)
        }

        var closeSongDeets = findViewById<ImageView>(R.id.closeSongDetails)
        closeSongDeets.setOnClickListener{
            var intents = Intent(this, MainActivity::class.java)
            startActivity(intents)
        }


//play button
        play.setOnClickListener {
            mp3.start()
            playSong(songList[currentSongIndex])
            initializeSeekBar()
            Toast.makeText(this, "Enjoy listening!!", Toast.LENGTH_LONG).show()
        }
//pause button
        pause.setOnClickListener {
            if (mp3.isPlaying) {
                mp3.pause()
            } else {
                Toast.makeText(this, "No music played :<", Toast.LENGTH_LONG).show()
            }
        }

//next song
        nextMusic.setOnClickListener {
            playNextSong(songList[currentSongIndex])
        }
//previous song
        previousMusic.setOnClickListener {
            playPreviousSong()
        }
    }

    //update
    private fun updatesongList(songInfo: Songinformation){
        Log.d("SongInformation List",""+songInfo)
        songTitleTextView.text = songInfo.name
        songArtistTextView.text = songInfo.artist
        songAlbum.setImageResource(songInfo.photoAlbum)

    }

    //play song it should show the current song details
    private fun playSong(songInfo: Songinformation) {
        if (::mp3.isInitialized) {
            mp3.start()
        }
        updatesongList(songList.get(currentSongIndex))

        val songTitleTextView: TextView = findViewById(R.id.txtMusicTitle)
        val songArtistTextView: TextView = findViewById(R.id.txtMusicArtist)
        val songAlbum: ImageView = findViewById(R.id.imgAlbum)
        songTitleTextView.text = songInfo.name
        songArtistTextView.text = songInfo.artist
        songAlbum.setImageResource(songInfo.photoAlbum)
    }

    //when the user press next song
    private fun playNextSong(songInfo: Songinformation) {
        if (currentSongIndex < songList.size - 1) {
            currentSongIndex++
        } else {
            currentSongIndex = 0
        }
        val songDetails = songList.get(currentSongIndex)
        updatesongList(songDetails)
        mp3.stop()

        mp3 = MediaPlayer.create(this, song[currentSongIndex])
        mp3.start()
        initializeSeekBar()
        Toast.makeText(this, "Enjoy listening!!", Toast.LENGTH_LONG).show()

    }

    //when the user press previous song
    private fun playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
        } else {
            currentSongIndex = songList.size - 1
        }
        val songDetails = songList.get(currentSongIndex)
        updatesongList(songDetails)
        mp3.stop()

        mp3 = MediaPlayer.create(this, song[currentSongIndex])
        mp3.start()
        initializeSeekBar()
        Toast.makeText(this, "Enjoy listening!!", Toast.LENGTH_LONG).show()
    }

    //seekbar
    private fun initializeSeekBar() {
        seekBar.max = mp3.duration

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mp3.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed for implementation but required by interface
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed for implementation but required by interface
            }
        })

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (mp3.isPlaying) {
                        updateSeekBarAndTimeBar()
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                handler.postDelayed(this, 1000)
            }
        }, 0)
    }
    //current position of the music time bar
    private fun updateSeekBarAndTimeBar() {

        seekBar.progress = mp3.currentPosition

        val currentPosition = mp3.currentPosition
        val minutes = (currentPosition / 1000) / 60
        val seconds = (currentPosition / 1000) % 60

        timeBar.text = String.format ("%02d:%02d", minutes, seconds)
    }

    // Inside onDestroy()
    override fun onDestroy() {
        super.onDestroy()
        mp3.release()


    }
}



