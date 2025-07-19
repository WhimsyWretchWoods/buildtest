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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import test.raku.util.zoomAndPan

@Composable
fun Player(uri: Uri, navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current // Get the current view for system UI access

    // Get the window instance from the activity context. Essential for system bar control.
    val window = (view.context as? Activity)?.window
        ?: throw IllegalStateException("Cannot find window from the current view's context")

    // Get the WindowInsetsController to manipulate system bars.
    val insetsController = remember(view) {
        WindowCompat.getInsetsController(window, view)
    }

    // Initialize ExoPlayer.
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    // State to control the visibility of player controls.
    var controlsVisible by remember { mutableStateOf(true) }

    // --- System Bar Management ---
    // Use DisposableEffect to hide system bars when this composable is active
    // and show them again when it's disposed.
    DisposableEffect(insetsController) {
        // Hide system bars (status bar and navigation bar)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Set behavior to allow bars to be transiently shown by system gestures (like swiping from edge).
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // When composable leaves composition, show system bars again.
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // --- ExoPlayer Lifecycle Management ---
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release() // Release player resources when not needed.
        }
    }

    // --- Auto-Hide Controls Logic ---
    // LaunchedEffect to hide controls automatically after a delay if they are visible.
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000L) // Wait for 3 seconds.
            controlsVisible = false // Hide controls.
        }
    }

    // Main Box layout to layer the video and controls.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { // Detect taps on the entire video surface.
                detectTapGestures {
                    controlsVisible = !controlsVisible // Toggle control visibility.
                    // When controls become visible, the LaunchedEffect above will restart its timer.
                    // If controls were already visible and user tapped, it hides them, timer stops.
                }
            }
    ) {
        // AndroidView to display the ExoPlayer video surface.
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We're using our custom controls.
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .zoomAndPan() // Apply zoom and pan to the video surface.
        )

        // AnimatedVisibility for the PlayerControls, making them fade in/out.
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(), // Controls fade in.
            exit = fadeOut(), // Controls fade out.
            modifier = Modifier.align(Alignment.BottomCenter) // Position controls at the bottom center.
        ) {
            PlayerControls(
                exoPlayer = exoPlayer,
                onPlayPauseClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    controlsVisible = true // Keep controls visible after interaction.
                },
                onStartOverClick = {
                    exoPlayer.seekTo(0)
                    exoPlayer.play()
                    controlsVisible = true // Keep controls visible after interaction.
                },
                onSubtitleClick = {
                    println("Subtitle button clicked")
                    controlsVisible = true // Keep controls visible after interaction.
                },
                onAudioClick = {
                    println("Audio button clicked")
                    controlsVisible = true // Keep controls visible after interaction.
                }
            )
        }
    }
}
