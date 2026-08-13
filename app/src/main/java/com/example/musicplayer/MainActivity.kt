package com.example.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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

enum class Screen {
    HOME, ALBUM, HISTORY, SETTINGS, PLAYER, ALBUM_SONGS
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

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAlbum by remember { mutableStateOf("") }
    var historyList by remember { mutableStateOf<List<MusicItem>>(emptyList()) }

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
        val item = currentItem
        if (isPlaying && item != null) {
            val player = PlayerController.get(context)
            while (isPlaying) {
                position = player.currentPosition
                duration = if (player.duration > 0) player.duration else item.duration
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
        currentScreen = Screen.PLAYER
        // 更新历史记录（去重，最多保留50首）
        historyList = listOf(item) + historyList.filter { it.id != item.id }.take(49)
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

    fun handleVolumeChange(volumeFraction: Float) {
        // 音量变化回调，可保存设置
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("需要音频权限", color = Color.White)
        }
    } else if (musicList.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到本地音乐", color = Color.White)
        }
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentScreen != Screen.PLAYER) {
                    NavigationBar(containerColor = Color(0xFF1A1A24)) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.HOME,
                            onClick = { currentScreen = Screen.HOME },
                            icon = { Text("主页", color = Color.White) },
                            label = { Text("主页", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.ALBUM,
                            onClick = { currentScreen = Screen.ALBUM },
                            icon = { Text("专辑", color = Color.White) },
                            label = { Text("专辑", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.HISTORY,
                            onClick = { currentScreen = Screen.HISTORY },
                            icon = { Text("历史", color = Color.White) },
                            label = { Text("历史", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.SETTINGS,
                            onClick = { currentScreen = Screen.SETTINGS },
                            icon = { Text("设置", color = Color.White) },
                            label = { Text("设置", color = Color.White) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen"
                ) { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            musicList = musicList,
                            currentItem = currentItem,
                            isPlaying = isPlaying,
                            onItemClick = { item, index -> playItem(item, index) },
                            onTogglePlay = { togglePlay() },
                            onOpenPlayer = { currentScreen = Screen.PLAYER },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it }
                        )
                        Screen.ALBUM -> AlbumScreen(
                            musicList = musicList,
                            onAlbumClick = { album ->
                                selectedAlbum = album
                                currentScreen = Screen.ALBUM_SONGS
                            }
                        )
                        Screen.ALBUM_SONGS -> AlbumSongsScreen(
                            album = selectedAlbum,
                            musicList = musicList,
                            currentItem = currentItem,
                            onItemClick = { item, index -> playItem(item, index) }
                        )
                        Screen.HISTORY -> HistoryScreen(
                            historyList = historyList,
                            currentItem = currentItem,
                            onItemClick = { item, index -> playItem(item, index) }
                        )
                        Screen.SETTINGS -> SettingsScreen()
                        Screen.PLAYER -> currentItem?.let { item ->
                            PlayerScreen(
                                item = item,
                                isPlaying = isPlaying,
                                position = position,
                                duration = duration,
                                onBack = {
                                    showPlayer = false
                                    currentScreen = Screen.HOME
                                },
                                onTogglePlay = { togglePlay() },
                                onNext = { nextTrack() },
                                onPrev = { prevTrack() },
                                onSeek = { seekTo(it) },
                                onVolumeChange = { handleVolumeChange(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
