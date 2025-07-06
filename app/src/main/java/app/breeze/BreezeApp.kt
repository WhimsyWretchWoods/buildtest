package app.breeze

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.breeze.data.ThemePreferenceRepository

@Composable
fun BreezeApp(themeRepository: ThemePreferenceRepository) {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.FOLDER_SCREEN,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable(AppRoutes.FOLDER_SCREEN) {
                FolderScreen(navController = navController)
            }
            composable(
                route = AppRoutes.IMAGE_SCREEN,
                arguments = listOf(
                    navArgument("folderPath") {type = NavType.StringType},
                    navArgument("folderName") {type = NavType.StringType}
                )
            ) { backStackEntry ->
                val folderPath = backStackEntry.arguments?.getString("folderPath") ?: "null"
                val folderName = backStackEntry.arguments?.getString("folderName") ?: "null"
                ImageScreen(navController = navController, folderPath, folderName)
            }
            composable(
                route = AppRoutes.FULL_SCREEN,
                arguments = listOf(navArgument("imageUri") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                FullScreen(imageUri, navController)
            }
            composable(AppRoutes.SETTINGS_SCREEN) {
                SettingsScreen(navController, themeRepository)
            }
        }
    }
}
