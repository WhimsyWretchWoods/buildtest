package test.raku

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Repeat
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
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer
) {
    // Internal state management for a more robust component
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var isAutoRepeatEnabled by remember { mutableStateOf(exoPlayer.repeatMode != Player.REPEAT_MODE_OFF) }
    var currentPosition by remember { mutableStateOf(exoPlayer.currentPosition) }
    var duration by remember { mutableStateOf(exoPlayer.duration) }
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableState of(false) }
    var selectedSubtitleTrack: Pair<TrackGroup, Int>? by remember { mutableStateOf(null) }
    var selectedAudioTrack: Pair<TrackGroup, Int>? by remember { mutableStateOf(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                // Correctly find the currently selected audio and subtitle tracks
                var newSelectedSubtitle: Pair<TrackGroup, Int>? = null
                var newSelectedAudio: Pair<TrackGroup, Int>? = null
                for (trackGroup in tracks.groups) {
                    if (trackGroup.isSelected) {
                        when (trackGroup.type) {
                            C.TRACK_TYPE_TEXT -> for (i in 0 until trackGroup.length) {
                                if (trackGroup.isTrackSelected(i)) {
                                    newSelectedSubtitle = Pair(trackGroup.mediaTrackGroup, i)
                                    break
                                }
                            }
                            C.TRACK_TYPE_AUDIO -> for (i in 0 until trackGroup.length) {
                                if (trackGroup.isTrackSelected(i)) {
                                    newSelectedAudio = Pair(trackGroup.mediaTrackGroup, i)
                                    break
                                }
                            }
                        }
                    }
                }
                selectedSubtitleTrack = newSelectedSubtitle
                selectedAudioTrack = newSelectedAudio
            }

            override fun onIsPlayingChanged(isPlayingChange: Boolean) {
                isPlaying = isPlayingChange
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                isAutoRepeatEnabled = repeatMode != Player.REPEAT_MODE_OFF
            }
        }
        exoPlayer.addListener(listener)
        // Initialize state on composition
        listener.onTracksChanged(exoPlayer.currentTracks)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(isPlaying, isSeeking) {
        while (isPlaying && !isSeeking) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            sliderPosition = currentPosition.toFloat()
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
            valueRange = 0f..duration.toFloat().coerceAtLeast(0f),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Replay button logic is now internal
            IconButton(onClick = { exoPlayer.seekTo(0) }) {
                Icon(Icons.Filled.Replay, contentDescription = "Start Over", tint = Color.White)
            }

            // Auto-repeat logic is now internal
            IconButton(onClick = {
                exoPlayer.repeatMode = if (isAutoRepeatEnabled) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
            }) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = "Auto Repeat",
                    tint = if (isAutoRepeatEnabled) MaterialTheme.colors.primary else Color.White
                )
            }

            // Play/Pause logic is now internal
            IconButton(onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() }) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }

            // Subtitle Menu
            Box {
                IconButton(onClick = { showSubtitleMenu = !showSubtitleMenu }) {
                    Icon(Icons.Filled.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                }
                DropdownMenu(expanded = showSubtitleMenu, onDismissRequest = { showSubtitleMenu = false }) {
                    // "Off" button that actually works
                    DropdownMenuItem(onClick = {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build()
                        showSubtitleMenu = false
                    }) {
                        Text("Off")
                        if (selectedSubtitleTrack == null) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    exoPlayer.currentTracks.groups
                        .filter { it.type == C.TRACK_TYPE_TEXT }
                        .forEach { tracksGroup ->
                            for (trackIndex in 0 until tracksGroup.length) {
                                val format = tracksGroup.getTrackFormat(trackIndex)
                                // Get human-readable track names
                                val trackName = if (!format.label.isNullOrBlank()) {
                                    format.label
                                } else if (!format.language.isNullOrBlank() && format.language != "und") {
                                    Locale.forLanguageTag(format.language!!).displayName
                                } else {
                                    "Track ${trackIndex + 1}"
                                }
                                val isSelected = selectedSubtitleTrack?.let { it.first == tracksGroup.mediaTrackGroup && it.second == trackIndex } ?: false
                                DropdownMenuItem(onClick = {
                                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                        .setOverrideForType(TrackSelectionOverride(tracksGroup.mediaTrackGroup, trackIndex))
                                        .build()
                                    showSubtitleMenu = false
                                }) {
                                    Text(trackName)
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }
                }
            }

            // Audio Menu
            Box {
                IconButton(onClick = { showAudioMenu = !showAudioMenu }) {
                    Icon(Icons.Filled.Audiotrack, contentDescription = "Audio Tracks", tint = Color.White)
                }
                DropdownMenu(expanded = showAudioMenu, onDismissRequest = { showAudioMenu = false }) {
                    // Audio "Off" button (disables audio)
                     DropdownMenuItem(onClick = {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                            .build()
                        showAudioMenu = false
                    }) {
                        Text("Off")
                        if (selectedAudioTrack == null) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    exoPlayer.currentTracks.groups
                        .filter { it.type == C.TRACK_TYPE_AUDIO }
                        .forEach { tracksGroup ->
                            for (trackIndex in 0 until tracksGroup.length) {
                                val format = tracksGroup.getTrackFormat(trackIndex)
                                val trackName = if (!format.label.isNullOrBlank()) {
                                    format.label
                                } else if (!format.language.isNullOrBlank() && format.language != "und") {
                                    Locale.forLanguageTag(format.language!!).displayName
                                } else {
                                    "Track ${trackIndex + 1}"
                                }
                                val isSelected = selectedAudioTrack?.let { it.first == tracksGroup.mediaTrackGroup && it.second == trackIndex } ?: false
                                DropdownMenuItem(onClick = {
                                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                                        .setOverrideForType(TrackSelectionOverride(tracksGroup.mediaTrackGroup, trackIndex))
                                        .build()
                                    showAudioMenu = false
                                }) {
                                    Text(trackName)
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    if (milliseconds < 0) return "00:00"
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
