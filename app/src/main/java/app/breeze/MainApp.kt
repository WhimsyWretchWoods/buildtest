package app.breeze

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

import app.breeze.AppRoutes
import app.breeze.AboutScreen
import app.breeze.FolderScreen
import app.breeze.ImageScreen
import app.breeze.FullScreen

import kotlinx.coroutines.launch
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.net.Uri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    label = {
                        Text("About")
                    },
                    selected = currentRoute == AppRoutes.ABOUT_SCREEN,
                    onClick = {
                        navController.navigate(AppRoutes.ABOUT_SCREEN) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                AppRoutes.FOLDER_SCREEN -> "Folders"
                                AppRoutes.IMAGE_SCREEN -> "?"
                                AppRoutes.FULL_SCREEN -> "?"
                                AppRoutes.ABOUT_SCREEN -> "About"
                                else -> "App"
                            }
                        )
                    },
                    navigationIcon = {
                        if (currentRoute != AppRoutes.FOLDER_SCREEN) {
                            IconButton(onClick = {
                                navController.navigateUp()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.FOLDER_SCREEN,
                modifier = Modifier.padding(innerPadding)
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
    }
}
