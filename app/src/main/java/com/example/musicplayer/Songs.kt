package com.example.musicplayer

import android.net.Uri

data class Songs(val id: Long, val name: String, val duration: String, val artist: String, val cover: Uri)