package com.example.musicplayer

import android.content.Context
import android.widget.MediaController

class MusicController(c: Context?) : MediaController(c) {
    override fun hide() {}
}
