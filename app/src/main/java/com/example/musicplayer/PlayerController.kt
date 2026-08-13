package com.example.musicplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

object PlayerController {

    private var player: ExoPlayer? = null

    fun get(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
        return player!!
    }

    fun play(context: Context, item: MusicItem) {
        val p = get(context)
        p.setMediaItem(MediaItem.fromUri(item.uri))
        p.prepare()
        p.play()
    }

    fun release() {
        player?.release()
        player = null
    }
}
