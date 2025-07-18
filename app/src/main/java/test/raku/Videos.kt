package test.raku

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Videos(navController: NavController) {
    val context = LocalContext.current
    val videoList = remember { mutableStateListOf<Uri>() }

    LaunchedEffect(Unit) {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA)
        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/storage/emulated/0/Samples/%")

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                videoList += uri
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        videoList.forEach { uri ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("player/${Uri.encode(uri.toString())}")
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Video URI:")
                    Text(uri.toString())
                }
            }
        }
    }
}
