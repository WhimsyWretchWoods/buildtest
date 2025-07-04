package app.breeze.data

import android.net.Uri

data class ImageFolder(
    val id: Long,
    val name: String,
    val path: String,
    val thumbnailUri: Uri
)
