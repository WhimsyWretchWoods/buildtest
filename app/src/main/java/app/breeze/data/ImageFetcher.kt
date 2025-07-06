package app.breeze.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageFetcher {

    suspend fun getFoldersWithImages(context: Context): List<ImageFolder> = withContext(Dispatchers.IO) {
        val folders = mutableMapOf<Long, ImageFolder>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bucketId = cursor.getLong(bucketIdCol)
                var bucketName = cursor.getString(bucketNameCol)
                val data = cursor.getString(dataCol)

                val file = File(data)
                val parentDir = file.parentFile
                val folderPath = parentDir?.absolutePath ?: ""

                if (bucketName.isNullOrBlank()) {
                    bucketName = parentDir?.name
                    if (bucketName.isNullOrBlank()) {
                        if (folderPath == "/storage/emulated/0" || folderPath == "/sdcard" || folderPath == context.filesDir.parentFile?.absolutePath) {
                            bucketName = "Internal Storage"
                        } else {
                            bucketName = "Other"
                        }
                    }
                }

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                val existingFolder = folders[bucketId]
                if (existingFolder == null || (existingFolder.name == "Internal Storage" || existingFolder.name == "Other") && bucketName != "Internal Storage" && bucketName != "Other") {
                    folders[bucketId] = ImageFolder(
                        id = bucketId,
                        name = bucketName,
                        path = folderPath,
                        thumbnailUri = contentUri
                    )
                }
            }
        }

        folders.values.sortedBy {
            it.name.lowercase()
        }
    }

    suspend fun getImagesInFolder(context: Context, folderPath: String): List<Uri> = withContext(Dispatchers.IO) {
        val imageUris = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        )

        val selection: String?
        val selectionArgs: Array<String>?

        val isRootFolder = folderPath == "/storage/emulated/0" || folderPath == "/sdcard" || folderPath == context.filesDir.parentFile?.absolutePath

        if (isRootFolder) {
            selection = "${MediaStore.Images.Media.DATA} LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?"
            selectionArgs = arrayOf(
                "$folderPath/%",
                "$folderPath%/%"
            )
        } else {
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            selectionArgs = arrayOf("$folderPath/%")
        }

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                imageUris.add(contentUri)
            }
        }
        imageUris
    }
}
