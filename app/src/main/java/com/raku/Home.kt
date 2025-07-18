package com.raku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var videoFolders by remember { mutableStateOf(emptyList<VideoFolder>()) }

    LaunchedEffect(Unit) {
        videoFolders = MediaStoreHelper.getVideoFolders(context)
    }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(videoFolders) { folder ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {navController.navigate(Screen.Videos.createRoute(folder.id))}) {
                    Text(text = folder.name)
                    Text(text = "${folder.count} ${if (folder.count == 1) "video" else "videos"}")
                }
            }
        }
    }