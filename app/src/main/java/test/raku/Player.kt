package test.raku

import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import test.raku.util.zoomAndPan

@Composable
fun Player(uri: Uri, navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current

    val window = (view.context as? Activity)?.window
        ?: throw IllegalStateException("Cannot find window from the current view's context")

    val insetsController = remember(view) {
        WindowCompat.getInsetsController(window, view)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    // Controls and system bars start visible.
    var controlsVisible by remember { mutableStateOf(true) }

    // Set initial system bar behavior and ensure they are visible.
    DisposableEffect(insetsController) {
        // Ensure system bars are visible initially
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        // Set behavior to allow bars to be transiently shown by system gestures (like swiping from edge).
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // When composable leaves composition, ensure system bars are shown again.
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    // Toggle controls visibility
                    controlsVisible = !controlsVisible

                    // Toggle system bars visibility based on controlsVisible state
                    if (controlsVisible) {
                        insetsController.show(WindowInsetsCompat.Type.systemBars())
                    } else {
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                    }
                }
            }
    ) {
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
                .zoomAndPan()
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerControls(
                exoPlayer = exoPlayer,
                onPlayPauseClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    // No need to set controlsVisible = true here, as tap handles it.
                    // If controls were hidden, a tap would show them and trigger this.
                    // If controls were visible, a tap on the button doesn't hide them.
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
}
