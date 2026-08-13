package com.example.musicplayer

import android.net.Uri

data class MusicItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val albumArtUri: Uri?,
    val duration: Long
)
