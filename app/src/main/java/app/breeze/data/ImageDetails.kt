package app.breeze.data

import android.net.Uri

data class ImageDetails(
    val uri: Uri,
    val name: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val path: String,
    val dateModified: Long
)
