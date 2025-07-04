package app.breeze

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import app.breeze.data.ImageFetcher
import app.breeze.data.ImageFolder
import app.breeze.AppRoutes

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(navController: NavController) {
    val context = LocalContext.current
    var folders by remember { mutableStateOf<List<ImageFolder>>(emptyList()) }

    LaunchedEffect(Unit) {
        folders = ImageFetcher.getFoldersWithImages(context)
    }

    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(folders) { folder ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable {
                        navController.navigate(AppRoutes.IMAGE_SCREEN.replace("{folderPath}", Uri.encode(folder.path)))

                    }
            ) {
                AsyncImage(
                    model = folder.thumbnailUri,
                    contentDescription = folder.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = folder.name,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}
