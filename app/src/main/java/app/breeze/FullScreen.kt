package app.breeze

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun FullScreen(imageUri: String) {
    val painter = rememberAsyncImagePainter(model = Uri.parse(imageUri))
    val zoomState = rememberZoomState(
        contentSize = painter.intrinsicSize
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState),
            contentScale = ContentScale.Fit
        )
    }
}
