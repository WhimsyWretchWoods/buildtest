package test.raku

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue // ADDED
import androidx.compose.runtime.mutableStateOf // ADDED
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // ADDED
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlin.math.max // ADDED
import kotlin.math.min // ADDED - Good practice even if not directly used for the max ambiguity

@Composable
fun Player(uri: Uri, navController: NavController) {
    val context = LocalContext.current

    // Zoom and Pan state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Animatable for smooth animation
    val animatedScale = remember { Animatable(scale) }
    val animatedOffsetX = remember { Animatable(offsetX) }
    val animatedOffsetY = remember { Animatable(offsetY) }

    // Store the size of the container (Box) for boundary calculations
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release() // Releasing the player
        }
    }

    // LaunchedEffect to animate scale changes
    LaunchedEffect(scale) {
        animatedScale.animateTo(scale, animationSpec = tween(150))
    }

    // LaunchedEffect to animate offset changes
    LaunchedEffect(offsetX, offsetY) {
        animatedOffsetX.animateTo(offsetX, animationSpec = tween(150))
        animatedOffsetY.animateTo(offsetY, animationSpec = tween(150))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { size ->
                containerSize = size // Get the size of the box holding the video
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f) // Limit zoom 1x to 5x

                    val newOffsetX = offsetX + pan.x * newScale
                    val newOffsetY = offsetY + pan.y * newScale

                    if (newScale > 1f) {
                        val scaledWidth = containerSize.width * newScale
                        val scaledHeight = containerSize.height * newScale

                        val maxPanX = max(0f, (scaledWidth - containerSize.width) / 2f)
                        val maxPanY = max(0f, (scaledHeight - containerSize.height) / 2f)

                        offsetX = newOffsetX.coerceIn(-maxPanX, maxPanX)
                        offsetY = newOffsetY.coerceIn(-maxPanY, maxPanY)
                    } else { // If zooming out or at 1x, reset offset to center
                        offsetX = 0f
                        offsetY = 0f
                    }

                    if (newScale <= 1.05f) { // Snap scale to 1x if very close
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    } else {
                        scale = newScale
                    }
                }
            }
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
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Video itself fits
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Apply animated scale and translation to the AndroidView holding the PlayerView
                    scaleX = animatedScale.value
                    scaleY = animatedScale.value
                    translationX = animatedOffsetX.value
                    translationY = animatedOffsetY.value
                }
        )
    }
}
