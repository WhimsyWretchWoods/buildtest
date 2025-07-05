package app.breeze

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.FOLDER_SCREEN
    ) {
        composable(AppRoutes.FOLDER_SCREEN) {
            FolderScreen(navController = navController)
        }
        composable(
            route = AppRoutes.IMAGE_SCREEN,
            arguments = listOf(navArgument("folderPath") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val folderPath = backStackEntry.arguments?.getString("folderPath") ?: ""
            ImageScreen(navController = navController, folderPath)
        }
        composable(
            route = AppRoutes.FULL_SCREEN,
            arguments = listOf(navArgument("imageUri") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            FullScreen(imageUri)
        }
        composable(AppRoutes.ABOUT_SCREEN) {
            AboutScreen()
        }
    }
}
