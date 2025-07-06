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

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val isRootFolder = folderPath == "/storage/emulated/0" || folderPath == "/sdcard" || folderPath == context.filesDir.parentFile?.absolutePath

        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$folderPath/%")


        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dataPath = cursor.getString(dataColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                if (isRootFolder) {
                    val file = File(dataPath)
                    if (file.parentFile?.absolutePath == folderPath) {
                        imageUris.add(contentUri)
                    }
                } else {
                    imageUris.add(contentUri)
                }
            }
        }
        imageUris
    }
    
    suspend fun getDetails(context: Context, imageUri: Uri): ImageDetails? {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        context.contentResolver.query(imageUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return ImageDetails(
                    uri = imageUri,
                    name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
                    width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)),
                    height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)),
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)),
                    dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                )
            }
        }
        return null
    }

}
