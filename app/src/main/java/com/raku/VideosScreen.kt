package com.raku

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun VideosScreen(folderId: String, navController: NavController) {
    val context = LocalContext.current
    var videosInFolder by remember { mutableStateOf(emptyList<VideoItem>()) }

    LaunchedEffect(folderId) {
        videosInFolder = MediaStoreHelper.getVideosInFolder(context, folderId)
    }
    LazyColumn {
        items(videosInFolder) {video -> 
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {navController.navigate(Screen.VideoPlayer.createRoute(video.uri))}) {
                Text(video.name)
                Text("${formatDuration(video.duration)}")
            }
        }
    }
}