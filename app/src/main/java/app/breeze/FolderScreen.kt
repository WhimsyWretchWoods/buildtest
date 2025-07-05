package app.breeze

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.breeze.data.ImageFetcher
import app.breeze.data.ImageFolder
import app.breeze.AppRoutes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.material.icons.filled.CheckCircle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun FolderScreen(navController: NavController) {
    val context = LocalContext.current
    var folders by remember { mutableStateOf<List<ImageFolder>>(emptyList()) }

    val selectedFolderPaths = remember { mutableStateListOf<String>() }
    var isInSelectionMode by remember { mutableStateOf(false) }

    fun toggleFolderSelection(folderPath: String) {
        if (selectedFolderPaths.contains(folderPath)) {
            selectedFolderPaths.remove(folderPath)
        } else {
            selectedFolderPaths.add(folderPath)
        }
        isInSelectionMode = selectedFolderPaths.isNotEmpty()
    }

    fun clearFolderSelection() {
        selectedFolderPaths.clear()
        isInSelectionMode = false
    }

    LaunchedEffect(Unit) {
        folders = ImageFetcher.getFoldersWithImages(context)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") },
                    selected = false,
                    onClick = {
                        navController.navigate(AppRoutes.ABOUT_SCREEN)
                        scope.launch { drawerState.close() }
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
                    title = { Text("Folders") },
                    navigationIcon = {
                        if (isInSelectionMode) {
                            IconButton(onClick = { clearFolderSelection() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                            }
                        } else {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isInSelectionMode,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    HorizontalFloatingToolbar(
                        actions = {
                            IconButton(onClick = {
                                println("Deleting folders: $selectedFolderPaths")
                                clearFolderSelection()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete selected folders")
                            }
                            IconButton(onClick = {
                                println("Sharing folders: $selectedFolderPaths")
                                clearFolderSelection()
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share selected folders")
                            }
                            IconButton(onClick = {
                                println("Info on folders: $selectedFolderPaths")
                            }) {
                                Icon(Icons.Default.Info, contentDescription = "Information")
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { clearFolderSelection() },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Default.Clear, "Exit selection mode")
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(folders) { folder ->
                    val isSelected = selectedFolderPaths.contains(folder.path)
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .combinedClickable(
                                onLongClick = {
                                    toggleFolderSelection(folder.path)
                                },
                                onClick = {
                                    if (isInSelectionMode) {
                                        toggleFolderSelection(folder.path)
                                    } else {
                                        navController.navigate(
                                            AppRoutes.IMAGE_SCREEN.replace(
                                                "{folderPath}",
                                                Uri.encode(folder.path)
                                            )
                                        )
                                    }
                                }
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = folder.thumbnailUri,
                                contentDescription = folder.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
