package com.example.musicplayer

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class EditSongActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener{
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        }

        var closeEditSong = findViewById<ImageView>(R.id.closeEdt)
        closeEditSong.setOnClickListener{
            var intent = Intent(this, SongView::class.java)
            startActivity(intent)
        }

    }


}

