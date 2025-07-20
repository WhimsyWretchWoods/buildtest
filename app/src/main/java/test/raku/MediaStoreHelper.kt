package test.raku

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object MediaStoreHelper {

    data class Video(
        val id: Long,
        val displayName: String,
        val path: String,
        val duration: Long
    )

    data class Folder(
        val id: String,
        val name: String,
        val videoCount: Int
    )

    private fun getAllVideosInternal(context: Context): List<VideoInternal> {
        val videoList = mutableListOf<VideoInternal>()
        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION
        )
        val queryUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.query(queryUri, projection, null, null, null)?.use { cursor ->
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val bucketDisplayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val videoIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val videoDisplayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val videoDataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val videoDurationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdColumn)
                val bucketDisplayName = cursor.getString(bucketDisplayNameColumn)
                val videoId = cursor.getLong(videoIdColumn)
                val videoDisplayName = cursor.getString(videoDisplayNameColumn)
                val videoPath = cursor.getString(videoDataColumn)
                val videoDuration = cursor.getLong(videoDurationColumn)

                if (bucketId != null && bucketDisplayName != null && videoDisplayName != null && videoPath != null) {
                    videoList.add(
                        VideoInternal(
                            videoId,
                            videoDisplayName,
                            videoPath,
                            videoDuration,
                            bucketId,
                            bucketDisplayName
                        )
                    )
                }
            }
        }
        return videoList
    }

    private data class VideoInternal(
        val id: Long,
        val displayName: String,
        val path: String,
        val duration: Long,
        val bucketId: String,
        val bucketDisplayName: String
    )

    fun getFoldersWithVideoCounts(context: Context): List<Folder> {
        val allVideos = getAllVideosInternal(context)
        val foldersMap = mutableMapOf<String, Int>()
        val folderNamesMap = mutableMapOf<String, String>()
        for (video in allVideos) {
            foldersMap[video.bucketId] = (foldersMap[video.bucketId] ?: 0) + 1
            folderNamesMap[video.bucketId] = video.bucketDisplayName
        }
        return foldersMap.map { (bucketId, count) ->
            Folder(bucketId, folderNamesMap[bucketId] ?: "Unknown Folder", count)
        }.sortedBy { it.name.lowercase() }
    }

    fun getVideosInFolder(context: Context, targetBucketId: String): List<Video> {
        return getAllVideosInternal(context)
            .filter { it.bucketId == targetBucketId }
            .sortedBy { it.displayName.lowercase() }
            .map {
                Video(
                    it.id,
                    it.displayName,
                    it.path,
                    it.duration
                )
            }
    }
}
