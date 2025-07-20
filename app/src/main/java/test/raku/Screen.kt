package test.raku

import android.net.Uri

sealed class Screen(val route: String) {
    object ListFolders: Screen("list_folders")
    object ListVideos: Screen("list_videos/{folderId}") {
        fun createRoute(folderId: String) = "list_videos/$folderId"
    }
    object Player: Screen("player/{videoUri}") {
        fun createRoute(videoUri: Uri) = "player/${Uri.encode(videoUri.toString())}"
    }
}
