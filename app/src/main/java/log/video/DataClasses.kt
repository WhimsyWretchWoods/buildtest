package log.video

import android.net.Uri

data class VideoFolder(
    val id: String,
    val name: String,
    val videoCount: Int
)

data class VideoItem(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val path: String
)
