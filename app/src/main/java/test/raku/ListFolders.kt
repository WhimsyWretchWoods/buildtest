package test.raku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext

@Composable
fun ListFolders(navController: NavController, modifier: Modifier) {
    
    val context = LocalContext.current
    var folders by remember { mutableStateOf(emptyList<MediaStoreHelper.Folder>()) }

    // Load folders when the Composable enters the composition
    LaunchedEffect(Unit) {
        folders = MediaStoreHelper.getFoldersWithVideoCounts(context)
    }


    LazyColumn () {
        items(folders) { folder ->
            Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable{navController.navigate(Screen.ListVideos.createRoute(folder.id))}) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Folder, 
                        contentDescription = "Folders",
                        modifier = Modifier.size(48.dp) 
                    )
                }
                Column {
                    Text(folder.name)
                    Text("${folder.videoCount}")
                }
            }
        }
    }
}
