package test.raku

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Subtitles
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
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Box // Added import for Box
import androidx.media3.common.Tracks // Added import for Tracks.Group

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    onPlayPauseClick: () -> Unit,
    onStartOverClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit
) {
    var isPlaying by remember(exoPlayer) {
        mutableStateOf(exoPlayer.playWhenReady && exoPlayer.playbackState == Player.STATE_READY)
    }

    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }

    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                isPlaying = playWhenReady && exoPlayer.playbackState == Player.STATE_READY
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isPlaying = exoPlayer.playWhenReady && playbackState == Player.STATE_READY
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking) {
                currentPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(0L)
                sliderPosition = currentPosition.toFloat()
            }
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatTime(currentPosition), color = Color.White)
            Text(text = formatTime(duration), color = Color.White)
        }

        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                isSeeking = true
                sliderPosition = newPosition
                currentPosition = newPosition.toLong()
            },
            onValueChangeFinished = {
                isSeeking = false
                exoPlayer.seekTo(sliderPosition.toLong())
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStartOverClick) {
                Icon(Icons.Filled.Replay, contentDescription = "Start Over", tint = Color.White)
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }

            Box {
                IconButton(onClick = { showSubtitleMenu = !showSubtitleMenu }) {
                    Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showSubtitleMenu,
                    onDismissRequest = { showSubtitleMenu = false }
                ) {
                    // Filter for text tracks from the currentTracks.groups
                    val textTrackGroups = exoPlayer.currentTracks.groups.filter {
                        it.type == C.TRACK_TYPE_TEXT
                    }

                    // "Disable" option for subtitles
                    DropdownMenuItem(onClick = {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                            .build()
                        showSubtitleMenu = false
                    }) {
                        Text("Disable")
                    }

                    // List available subtitle tracks
                    textTrackGroups.forEach { trackGroup: Tracks.Group -> // Explicitly cast to Tracks.Group
                        for (trackIndex in 0 until trackGroup.length) {
                            // Use getTrackFormat on Tracks.Group to get the Format
                            val format = trackGroup.getTrackFormat(trackIndex)
                            DropdownMenuItem(onClick = {
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                    .setOverrideForType(
                                        // Pass the actual TrackGroup from the Tracks.Group and a list with the track index
                                        TrackSelectionOverride(trackGroup.trackGroup, listOf(trackIndex))
                                    )
                                    .build()
                                showSubtitleMenu = false
                            }) {
                                Text(format.language ?: format.id ?: "Track ${trackIndex + 1}")
                            }
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { showAudioMenu = !showAudioMenu }) {
                    Icon(Icons.Filled.Audiotrack, contentDescription = "Audio Tracks", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showAudioMenu,
                    onDismissRequest = { showAudioMenu = false }
                ) {
                    // Filter for audio tracks from the currentTracks.groups
                    val audioTrackGroups = exoPlayer.currentTracks.groups.filter {
                        it.type == C.TRACK_TYPE_AUDIO
                    }

                    // "Disable" option for audio
                    DropdownMenuItem(onClick = {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                            .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                            .build()
                        showAudioMenu = false
                    }) {
                        Text("Disable")
                    }

                    // List available audio tracks
                    audioTrackGroups.forEach { trackGroup: Tracks.Group -> // Explicitly cast to Tracks.Group
                        for (trackIndex in 0 until trackGroup.length) {
                            // Use getTrackFormat on Tracks.Group to get the Format
                            val format = trackGroup.getTrackFormat(trackIndex)
                            DropdownMenuItem(onClick = {
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                    .setOverrideForType(
                                        // Pass the actual TrackGroup from the Tracks.Group and a list with the track index
                                        TrackSelectionOverride(trackGroup.trackGroup, listOf(trackIndex))
                                    )
                                    .build()
                                showAudioMenu = false
                            }) {
                                Text(format.language ?: format.id ?: "Track ${trackIndex + 1}")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
