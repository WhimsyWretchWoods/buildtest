package com.raku

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerScreen(videoUriString: String) {
    val context = LocalContext.current
    val videoUri = remember(videoUriString) { Uri.decode(videoUriString).toUri() } // Decode URI string

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true // Start playing automatically
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // AndroidView allows you to embed traditional Android Views into Compose
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    // Optional: customize player view properties here
                    // useController = true
                    // setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Extension function to convert String to Uri (if not already available)
private fun String.toUri(): Uri {
    return Uri.parse(this)
}