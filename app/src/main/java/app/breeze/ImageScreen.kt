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

import app.breeze.AppRoutes
import app.breeze.data.ImageFetcher

import androidx.navigation.NavController

@Composable
fun ImageScreen(navController: NavController, folderPath: String) {
    val context = LocalContext.current
    var images by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    LaunchedEffect(folderPath) {
        images = ImageFetcher.getImagesInFolder(context, folderPath)
    }

    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(images) {
            uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                .padding(2.dp)
                .aspectRatio(1f)
                .clickable {
                    navController.navigate(
                        AppRoutes.FULL_SCREEN.replace("{imageUri}", Uri.encode(uri.toString()))
                    )
                },
                contentScale = ContentScale.Crop
            )
        }
    }

}
