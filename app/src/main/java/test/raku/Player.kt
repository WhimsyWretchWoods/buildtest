package test.raku

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
// Import the Zoomable composable
import com.engawapg.lib.zoomable.rememberZoomState
import com.engawapg.lib.zoomable.zoomable

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
            exoPlayer.release() // Release the player
        }
    }

    // Initialize the ZoomState for the Zoomable modifier
    val zoomState = rememberZoomState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Apply the zoomable modifier here
            .zoomable(zoomState) // This handles all the touch input, scaling, and panning
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // No built-in ExoPlayer controls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Video itself fits within the AndroidView
                }
            },
            modifier = Modifier.fillMaxSize()
            // NO graphicsLayer needed here, Zoomable handles transformations
        )
    }
}
