package com.example.musicplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AlbumSummary(
    val name: String,
    val songCount: Int,
    val artist: String
)

@Composable
fun AlbumScreen(
    musicList: List<MusicItem>,
    onAlbumClick: (String) -> Unit
) {
    val albums = remember(musicList) {
        musicList.groupBy { it.album }.map { (album, songs) ->
            AlbumSummary(album, songs.size, songs.first().artist)
        }
    }

    LazyColumn(Modifier.fillMaxSize()) {
        items(albums) { album ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onAlbumClick(album.name) }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            album.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${album.artist} · ${album.songCount}首",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumSongsScreen(
    album: String,
    musicList: List<MusicItem>,
    currentItem: MusicItem?,
    onItemClick: (MusicItem, Int) -> Unit
) {
    val songs = remember(musicList, album) { musicList.filter { it.album == album } }
    LazyColumn(Modifier.fillMaxSize()) {
        items(songs) { item ->
            MusicRow(
                item = item,
                isCurrent = item.id == currentItem?.id,
                onClick = { onItemClick(item, musicList.indexOf(item)) }
            )
        }
    }
}
