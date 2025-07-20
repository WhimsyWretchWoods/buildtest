package test.raku

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.material.Text
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import android.net.Uri
import androidx.core.net.toUri

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.ListFolders.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(Screen.ListFolders.route) {
            ListFolders(navController = navController)
        }
        composable(
            route = Screen.ListVideos.route,
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            if (folderId != null) {
                ListVideos(navController = navController, folderId = folderId)
            } else {
                Text("Error: Folder ID not found.")
            }
        }
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoUriString = backStackEntry.arguments?.getString("videoUri")
            if (videoUriString != null) {
                val videoUri = Uri.decode(videoUriString).toUri()
                Player(uri = videoUri)
            } else {
                Text("Error: Video URI not found.")
            }
        }
    }
}
