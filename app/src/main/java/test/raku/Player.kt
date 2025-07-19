package test.raku

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import test.raku.util.zoomAndPan
import androidx.compose.ui.Alignment // Import for Alignment

@Composable
fun Player(uri: Uri, navController: NavController) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box( // Changed from Column to Box to layer elements
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video surface, now directly inside the main Box.
        // It still gets the zoomAndPan modifier.
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .zoomAndPan() // zoomAndPan applies to the AndroidView itself
        )

        // Player controls, layered on top of the video.
        // Aligned to the bottom center of the Box.
        PlayerControls(
            modifier = Modifier.align(Alignment.BottomCenter), // Align to bottom center
            exoPlayer = exoPlayer,
            onPlayPauseClick = {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            },
            onStartOverClick = {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            },
            onSubtitleClick = {
                println("Subtitle button clicked")
            },
            onAudioClick = {
                println("Audio button clicked")
            }
        )
    }
}
