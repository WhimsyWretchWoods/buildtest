package app.breeze

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.toSize
import coil.compose.rememberAsyncImagePainter
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun FullScreen(imageUri: String) {
    val painter = rememberAsyncImagePainter(model = Uri.parse(imageUri))
    val zoomState = rememberZoomState(
        contentSize = painter.intrinsicSize.takeIf { it.width > 0f && it.height > 0f } ?: remember { androidx.compose.ui.geometry.Size.Zero }
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
