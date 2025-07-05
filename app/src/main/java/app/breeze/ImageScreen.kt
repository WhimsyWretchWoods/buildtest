package app.breeze

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll
import app.breeze.data.ImageFetcher
import app.breeze.ui.components.ConfirmDeleteDialog
import app.breeze.ui.components.InfoDialog
import app.breeze.ui.components.SelectionFloatingToolbar
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImageScreen(navController: NavController, folderPath: String) {
    val context = LocalContext.current
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val selectedImageUris = remember { mutableStateListOf<String>() }
    var isInSelectionMode by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

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
                        Spacer(Modifier.width(0.dp))
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
            SelectionFloatingToolbar(
                isInSelectionMode = isInSelectionMode,
                onClearSelection = { clearImageSelection() },
                onDeleteClick = { showDeleteConfirmationDialog = true },
                onInfoClick = { showInfoDialog = true }
            )
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

    InfoDialog(
        showDialog = showInfoDialog,
        onDismiss = { showInfoDialog = false },
        title = "Image Information",
        infoItems = selectedImageUris.toList()
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
