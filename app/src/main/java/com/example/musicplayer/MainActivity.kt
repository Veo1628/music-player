package com.example.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicApp()
        }
    }
}

private fun hasAudioPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}

@Composable
fun MusicApp() {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var musicList by remember { mutableStateOf<List<MusicItem>>(emptyList()) }

    var currentItem by remember { mutableStateOf<MusicItem?>(null) }
    var currentIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPlayer by remember { mutableStateOf(false) }

    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            val perms = if (Build.VERSION.SDK_INT >= 33) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(perms)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            withContext(Dispatchers.IO) {
                musicList = MusicRepository.loadMusic(context)
            }
        }
    }

    LaunchedEffect(isPlaying, currentItem) {
        if (isPlaying && currentItem != null) {
            val player = PlayerController.get(context)
            while (isPlaying) {
                position = player.currentPosition
                duration = if (player.duration > 0) player.duration else currentItem.duration
                delay(500)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            PlayerController.release()
        }
    }

    fun playItem(item: MusicItem, index: Int) {
        PlayerController.play(context, item)
        currentItem = item
        currentIndex = index
        isPlaying = true
        position = 0
        duration = item.duration
        showPlayer = true
    }

    fun togglePlay() {
        val player = PlayerController.get(context)
        if (player.isPlaying) {
            player.pause()
            isPlaying = false
        } else {
            player.play()
            isPlaying = true
        }
    }

    fun nextTrack() {
        if (musicList.isNotEmpty()) {
            val next = (currentIndex + 1) % musicList.size
            playItem(musicList[next], next)
        }
    }

    fun prevTrack() {
        if (musicList.isNotEmpty()) {
            val prev = if (currentIndex - 1 < 0) musicList.size - 1 else currentIndex - 1
            playItem(musicList[prev], prev)
        }
    }

    fun seekTo(fraction: Float) {
        val player = PlayerController.get(context)
        val newPos = (fraction * duration).toLong()
        player.seekTo(newPos)
        position = newPos
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("需要音频权限", color = Color.White)
        }
    } else if (musicList.is
