package log.video

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Sd
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView

@Composable
fun FolderList(navController: NavController) {
    val context = LocalContext.current
    val folders = remember {
        MediaStoreHelper.getVideoFolders(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(folders) {
                folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Changed to navigate with folder.id, not folder.path
                        navController.navigate("video_list?folderId=${Uri.encode(folder.id)}")
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.Folder, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(folder.name)
                    Text("${folder.videoCount} videos")
                }
            }
        }
    }
}


@Composable
fun VideoList(navController: NavController, folderId: String) { // Added navController
    val context = LocalContext.current
    val videos = remember(folderId) {
        MediaStoreHelper.getVideosInFolder(context, folderId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(videos) {
                video ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("video_player?videoUri=${Uri.encode(video.uri.toString())}")
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(modifier = Modifier.size(64.dp)) {
                    AsyncImage(
                        model = video.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(video.name)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(formatDuration(video.duration))
                        Spacer(modifier = Modifier.width(8.dp))
                        val resolutionIcon = when {
                            MediaStoreHelper.isHD(video.width, video.height) -> {
                                Icon(Icons.Filled.Hd, contentDescription = "HD", modifier = Modifier.size(24.dp))
                            }
                            MediaStoreHelper.isSD(video.width, video.height) -> {
                                Icon(Icons.Filled.Sd, contentDescription = "SD", modifier = Modifier.size(24.dp))
                            } else -> {
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                        }
                        if (resolutionIcon != null) {
                            resolutionIcon
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUri: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(
        AndroidView(factory = { ctx ->
            androidx.media3.ui.PlayerView(ctx).apply {
                player = exoPlayer
            }
        }, modifier = Modifier.fillMaxSize())
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}
