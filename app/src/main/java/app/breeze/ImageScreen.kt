package app.breeze

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll
import app.breeze.data.ImageFetcher
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.CheckCircle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageScreen(navController: NavController, folderPath: String) {
    val context = LocalContext.current
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val selectedImageUris = remember { mutableStateListOf<String>() }
    var isInSelectionMode by remember { mutableStateOf(false) }

    fun toggleImageSelection(imageUri: String) {
        if (selectedImageUris.contains(imageUri)) {
            selectedImageUris.remove(imageUri)
        } else {
            selectedImageUris.add(imageUri)
        }
        isInSelectionMode = selectedImageUris.isNotEmpty()
    }

    fun clearImageSelection() {
        selectedImageUris.clear()
        isInSelectionMode = false
    }

    LaunchedEffect(folderPath) {
        images = ImageFetcher.getImagesInFolder(context, folderPath)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(folderPath) },
                navigationIcon = {
                    if (isInSelectionMode) {
                        IconButton(onClick = { clearImageSelection() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                        }
                    } else {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            println("Deleting images: $selectedImageUris")
                            clearImageSelection()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected images")
                        }
                        IconButton(onClick = {
                            println("Sharing images: $selectedImageUris")
                            clearImageSelection()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share selected images")
                        }
                        IconButton(onClick = {
                            println("Info on images: $selectedImageUris")
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "Image info")
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { clearImageSelection() },
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
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(images) { uri ->
                val isSelected = selectedImageUris.contains(uri.toString())
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .combinedClickable(
                            onLongClick = {
                                toggleImageSelection(uri.toString())
                            },
                            onClick = {
                                if (isInSelectionMode) {
                                    toggleImageSelection(uri.toString())
                                } else {
                                    navController.navigate(
                                        AppRoutes.FULL_SCREEN.replace(
                                            "{imageUri}",
                                            Uri.encode(uri.toString())
                                        )
                                    )
                                }
                            }
                        )
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            } else Modifier
                        )
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
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
