package log.video

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File // This import is not strictly needed for the fix, but was in your previous code

data class VideoFolder(
    val id: String,
    val name: String, // Keep as non-null if you want to enforce a name
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

object MediaStoreHelper {

    fun getVideoFolders(context: Context): List<VideoFolder> {
        val folderMap = mutableMapOf<String, Pair<String, Int>>()

        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use {
            cursor ->
            val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdIndex)
                // Handle potential null for bucketName
                val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown Folder" // Provide a default name

                if (bucketId != null) {
                    val currentCount = folderMap[bucketId]?.second ?: 0
                    folderMap[bucketId] = Pair(bucketName, currentCount + 1)
                } else {
                    Log.w("MediaStoreHelper", "Found media item with null BUCKET_ID. Skipping.")
                }
            }
        }

        return folderMap.map {
            (id, pair) ->
            VideoFolder(id = id, name = pair.first, videoCount = pair.second)
        }.sortedBy {
            it.name.lowercase()
        }
    }

    fun getVideosInFolder(context: Context, folderId: String): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA
        )

        val selection = "${MediaStore.Video.Media.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(folderId)

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use {
            cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val wCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val hCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: "Unknown Video"
                val duration = cursor.getLong(durCol)
                val width = cursor.getInt(wCol)
                val height = cursor.getInt(hCol)
                val path = cursor.getString(dataCol) ?: ""
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())

                videos.add(VideoItem(uri, name, duration, width, height, path))
            }
        }

        return videos
    }

    fun isHD(width: Int, height: Int): Boolean {
        return width == 1280 && height == 720
    }

    fun isSD(width: Int, height: Int): Boolean {
        return (width > 0 && height > 0) && (width < 1280 && height < 720)
    }
}