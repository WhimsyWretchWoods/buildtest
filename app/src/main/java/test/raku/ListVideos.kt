package test.raku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.concurrent.TimeUnit
import java.io.File
import android.net.Uri 

@Composable
fun ListVideos(navController: NavController, folderId: String, modifier: Modifier) {
    val context = LocalContext.current
    var videos by remember { mutableStateOf(emptyList<MediaStoreHelper.Video>()) }

    LaunchedEffect(Unit) {
        videos = MediaStoreHelper.getVideosInFolder(context, folderId)
    }
    
    LazyColumn () {
        items(videos) { video ->
            Row(modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clickable{
                    val videoFile = File(video.path)
                    val videoUri = Uri.fromFile(videoFile)
                    navController.navigate(Screen.Player.createRoute(videoUri))
                } ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Videocam,
                        contentDescription = "Video",
                        modifier = Modifier.size(48.dp)
                    )
                }
                Column {
                    Text(video.displayName)
                    Text("${formatDuration(video.duration)}")
                }
            }
        }
    }
}

fun formatDuration(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
