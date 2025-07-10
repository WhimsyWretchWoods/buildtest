package log.video

fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
           else "%02d:%02d".format(m, s)
}
