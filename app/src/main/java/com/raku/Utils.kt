package com.raku

import java.util.concurrent.TimeUnit

fun formatDuration(duration: Long): String {
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
