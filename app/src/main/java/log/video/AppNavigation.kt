package log.video

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val isVideoPlayerScreen = currentRoute?.startsWith("video_player") == true

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                    },
                    label = {
                        Text("Settings")
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isVideoPlayerScreen) {
                    TopAppBar(
                        title = {
                            Text("Video")
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0.dp)
        ) {
            innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "folder_list",
                modifier = Modifier.padding(innerPadding),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable(
                    "folder_list"
                ) {
                    FolderList(navController)
                }
                composable(
                    route = "video_list?folderId={folderId}",
                    arguments = listOf(navArgument("folderId") {
                        type = NavType.StringType
                    })
                ) {
                    backStackEntry ->
                    val folderId = backStackEntry.arguments?.getString("folderId")!!
                    VideoList(navController = navController, folderId = folderId)
                }
                composable(
                    route = "video_player?videoUri={videoUri}",
                    arguments = listOf(navArgument("videoUri") {
                        type = NavType.StringType
                    })
                ) {
                    backStackEntry ->
                    val videoUri = backStackEntry.arguments?.getString("videoUri")!!
                    VideoPlayer(videoUri = videoUri)
                }
            }
        }
    }
}
