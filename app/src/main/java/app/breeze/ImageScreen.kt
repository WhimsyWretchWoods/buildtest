package app.breeze

import androidx.compose.foundation.lazy.grid.*
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(navController: NavController, folderPath: String) {
    val context = LocalContext.current
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }

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
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(images) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .clickable {
                            navController.navigate(
                                AppRoutes.FULL_SCREEN.replace(
                                    "{imageUri}",
                                    Uri.encode(uri.toString())
                                )
                            )
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
