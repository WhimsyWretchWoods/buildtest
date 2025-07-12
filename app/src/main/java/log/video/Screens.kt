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
import androidx.compose.ui.layout.ContentScale
import android.util.TypedValue
import androidx.media3.ui.CaptionStyleCompat
import android.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import android.view.View

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
                        navController.navigate("video_list?folderId=${Uri.encode(folder.id)}&folderDisplayName=${Uri.encode(folder.name)}")
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
                    Text("${folder.videoCount} ${if (folder.videoCount == 1) "video" else "videos"}")
                }
            }
        }
    }
}

@Composable
fun VideoList(navController: NavController, folderId: String, folderDisplayName: String) {
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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
                        resolutionIcon
                    }
                }
            }
        }
    }
}
@Composable
fun VideoPlayer(videoUri: String) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalContext.current as ComponentActivity
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.playWhenReady = true
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            exoPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (window != null) {
        DisposableEffect(window) {
            val insetsController = WindowCompat.getInsetsController(window, view)

            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            onDispose {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowSubtitleButton(true)
                setBackgroundColor(Color.BLACK)

                val subtitleView = this.subtitleView

                subtitleView?.apply {
                    setPadding(16, 16, 16, 16)

                    val textSizeSp = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        16f
                    } else {
                        24f
                    }
                    setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)

                    val customCaptionStyle = CaptionStyleCompat(
                        Color.WHITE,
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                        Color.BLACK,
                        null
                    )
                    setStyle(customCaptionStyle)
                }
                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                    if (window != null) {
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        if (visibility == View.VISIBLE) {
                            insetsController.show(WindowInsetsCompat.Type.systemBars())
                        } else {
                            insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        }
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
