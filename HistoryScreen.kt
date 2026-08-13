package com.example.musicplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(
    historyList: List<MusicItem>,
    currentItem: MusicItem?,
    onItemClick: (MusicItem, Int) -> Unit
) {
    if (historyList.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无播放历史", color = Color.White.copy(alpha = 0.7f))
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            items(historyList) { item ->
                MusicRow(
                    item = item,
                    isCurrent = item.id == currentItem?.id,
                    onClick = { onItemClick(item, historyList.indexOf(item)) }
                )
            }
        }
    }
}
