package test.raku

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.net.Uri
import androidx.compose.material.Text

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
            ListFolders(
                navController = navController,
                modifier = Modifier
                    .padding(WindowInsets.statusBars.asPaddingValues())
            )
        }
        composable(
            route = Screen.ListVideos.route,
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            if (folderId != null) {
                ListVideos(
                    navController = navController,
                    folderId = folderId,
                    modifier = Modifier
                        .padding(WindowInsets.statusBars.asPaddingValues())
                )
            } else {
                Text("Error: Folder ID not found.")
            }
        }
        composable(
            route = Screen.PlayerScreen.route, // Corrected route name
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
