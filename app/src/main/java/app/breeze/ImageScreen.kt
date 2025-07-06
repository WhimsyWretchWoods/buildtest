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
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll

import app.breeze.data.ImageFetcher
import app.breeze.data.ImageDetails
import app.breeze.ui.components.ConfirmDeleteDialog
import app.breeze.ui.components.InfoDialog
import app.breeze.ui.components.SelectionFloatingToolbar

import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageScreen(navController: NavController, folderPath: String, folderName: String) {
    val context = LocalContext.current
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val selectedImageUris = remember { mutableStateListOf<String>() }
    var isInSelectionMode by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    
    var imageDetailsToShow by remember { mutableStateOf<List<ImageDetails>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

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
            LargeFlexibleTopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                expandedHeight = 150.dp,
                collapsedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SelectionFloatingToolbar(
                    isInSelectionMode = isInSelectionMode,
                    onClearSelection = { clearImageSelection() },
                    onDeleteClick = { showDeleteConfirmationDialog = true },
                    onInfoClick = { 
                        coroutineScope.launch {
                            imageDetailsToShow = selectedImageUris.mapNotNull { uriString ->
                                ImageFetcher.getDetails(context, Uri.parse(uriString))
                            }
                            showInfoDialog = true
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
                        ),
                    contentAlignment = Alignment.TopEnd
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
                            modifier = Modifier
                                .padding(4.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    }

    InfoDialog(
        showDialog = showInfoDialog,
        onDismiss = { showInfoDialog = false },
        imageDetailsList = imageDetailsToShow
    )

    ConfirmDeleteDialog(
        showDialog = showDeleteConfirmationDialog,
        onDismiss = { showDeleteConfirmationDialog = false },
        onConfirm = {
            println("User confirmed deletion of images: $selectedImageUris")
            clearImageSelection()
            showDeleteConfirmationDialog = false
        },
        itemCount = selectedImageUris.size,
        itemType = "image"
    )
}
