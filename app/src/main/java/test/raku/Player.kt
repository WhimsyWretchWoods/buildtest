package test.raku

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background

import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs

import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

import androidx.media3.common.Tracks
import androidx.media3.common.C
import android.widget.Toast
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import androidx.media3.ui.SubtitleView
import android.graphics.Typeface
import androidx.media3.ui.CaptionStyleCompat

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Player() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoUri = MediaStoreHelper.getSampleVideoUri(context)

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var isFabVisible by remember { mutableStateOf(true) }
    
    var wasPlaying by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    
    var showOptions by remember { mutableStateOf(false) }
    val selected by remember { mutableStateOf(true) }
    
    var showNoSubtitlesToast by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offset += offsetChange * scale
        } else {
            offset = Offset.Zero
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            videoUri?.let { uri ->
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(currentIsPlaying: Boolean) {
                isPlaying = currentIsPlaying
            }
            override fun onTracksChanged(tracks: Tracks) {
                 val hasTextTracks =  tracks.groups.any { trackGroup ->
                     trackGroup.type == C.TRACK_TYPE_TEXT && trackGroup.length > 0
                 }
                 showNoSubtitlesToast = !hasTextTracks
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                wasPlaying = exoPlayer.isPlaying
                exoPlayer.pause()
            }
            override fun onResume(owner: LifecycleOwner) {
                if (wasPlaying) {
                    exoPlayer.play()
                }
            }
            override fun onDestroy(owner: LifecycleOwner) {
                exoPlayer.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (showOptions && !isFabVisible) {
                        showOptions = false
                    } else {
                        isFabVisible = !isFabVisible
                    }
                }
            }
            .fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            }
        )

        if (isFabVisible) {
            LargeFloatingActionButton(
                onClick = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                )
            }

            HorizontalFloatingToolbar (
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp),
                expanded = true,
                content = {
                    IconButton(onClick = {
                        if (showNoSubtitlesToast) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "No subtitles found.",
                                    actionLabel = "Dismiss",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {
                            showOptions = true
                            isFabVisible = false
                        }
                    }) {
                        Icon(Icons.Filled.Subtitles, contentDescription = null)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                    }
                }
            )
        }

        if (showOptions) {
            Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp, end = 16.dp)) {
                ElevatedFilterChip(
                    selected = false,
                    onClick = {},
                    label = {Text("Small")},
                    modifier = Modifier
                        .padding(8.dp)
                )
                ElevatedFilterChip(
                    selected = selected,
                    onClick = {},
                    label = {Text("Medium")},
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Done, contentDescription = null) }
                    } else {
                        null
                    },
                    modifier = Modifier.padding(8.dp)
                )
                ElevatedFilterChip(
                    selected = false,
                    onClick = {},
                    label = {Text("Large")},
                    modifier = Modifier.padding(8.dp)
                )
                
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
