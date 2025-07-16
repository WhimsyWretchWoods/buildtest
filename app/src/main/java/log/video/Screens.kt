package log.video

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannedString
import android.util.TypedValue
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Sd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.text.Cue
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb

@Composable
fun FolderList(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var folders by remember { mutableStateOf(emptyList<VideoFolder>()) }

    val loadFolders: () -> Unit = remember {
        {
            folders = MediaStoreHelper.getVideoFolders(context)
        }
    }

    DisposableEffect(lifecycleOwner) {
        loadFolders()

        val mediaStoreObserver = MediaStoreContentObserver(context) {
            loadFolders()
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                loadFolders()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            context.contentResolver.unregisterContentObserver(mediaStoreObserver)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(folders) {
                folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("video_list?folderId=${Uri.encode(folder.id)}&folderDisplayName=${Uri.encode(folder.name)}")
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Filled.Folder, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(folder.name)
                    Text("${folder.videoCount} ${if (folder.videoCount == 1) "video" else "videos"}")
                }
            }
        }
    }
}

@Composable
fun VideoList(navController: NavController, folderId: String, folderDisplayName: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var videos by remember { mutableStateOf(emptyList<VideoItem>()) }

    val loadVideos: () -> Unit = remember(folderId) {
        {
            videos = MediaStoreHelper.getVideosInFolder(context, folderId)
        }
    }

    DisposableEffect(lifecycleOwner, folderId) {
        loadVideos()

        val mediaStoreObserver = MediaStoreContentObserver(context) {
            loadVideos()
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                loadVideos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            context.contentResolver.unregisterContentObserver(mediaStoreObserver)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(videos) {
                video ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("video_player?videoUri=${Uri.encode(video.uri.toString())}")
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(modifier = Modifier.size(64.dp)) {
                    AsyncImage(
                        model = video.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(video.name)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(formatDuration(video.duration))
                        Spacer(modifier = Modifier.width(8.dp))
                        val resolutionIcon = when {
                            MediaStoreHelper.isHD(video.width, video.height) -> {
                                Icon(Icons.Filled.Hd, contentDescription = "HD", modifier = Modifier.size(24.dp))
                            }
                            MediaStoreHelper.isSD(video.width, video.height) -> {
                                Icon(Icons.Filled.Sd, contentDescription = "SD", modifier = Modifier.size(24.dp))
                            } else -> {
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                        }
                        resolutionIcon
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUri: String) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalContext.current as ComponentActivity
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.playWhenReady = true
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            exoPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (window != null) {
        DisposableEffect(window) {
            val insetsController = WindowCompat.getInsetsController(window, view)

            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            onDispose {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowSubtitleButton(true)
                setBackgroundColor(AndroidColor.BLACK)

                val subtitleView = this.subtitleView

                subtitleView?.apply {
                    setPadding(16, 16, 16, 16)

                    val textSizeSp = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        16f
                    } else {
                        24f
                    }
                    setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)

                    val prefs = context.getSharedPreferences("subtitle_settings", Context.MODE_PRIVATE)
                    val fgColor = prefs.getInt("subtitle_fg_color", AndroidColor.WHITE)
                    val bgColor = prefs.getInt("subtitle_bg_color", AndroidColor.TRANSPARENT)
                    val edgeType = prefs.getInt("subtitle_edge_type", CaptionStyleCompat.EDGE_TYPE_OUTLINE)
                    val edgeColor = prefs.getInt("subtitle_edge_color", AndroidColor.BLACK)
                    val fontPath = prefs.getString("subtitle_font_path", null)

                    val typeface = if (fontPath != null) {
                        try { Typeface.createFromAsset(context.assets, fontPath) }
                        catch (e: Exception) { Typeface.DEFAULT }
                    } else { Typeface.DEFAULT }

                    val customCaptionStyle = CaptionStyleCompat(
                        fgColor,
                        bgColor,
                        AndroidColor.TRANSPARENT,
                        edgeType,
                        edgeColor,
                        typeface
                    )
                    setStyle(customCaptionStyle)
                }
                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                    if (window != null) {
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        if (visibility == View.VISIBLE) {
                            insetsController.show(WindowInsetsCompat.Type.systemBars())
                        } else {
                            insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        }
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SettingsView(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Appearance",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .clickable {
                        navController.navigate("subtitle_settings")
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Subtitle", style = MaterialTheme.typography.titleMedium)
                        Text("Customize your subtitles", style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go to Subtitle Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleStyleSettingsView() {
    val context = LocalContext.current

    var currentForegroundColor by remember { mutableStateOf(ComposeColor.White) }
    var currentBackgroundColor by remember { mutableStateOf(ComposeColor.Black) }
    var currentEdgeType by remember { mutableStateOf(CaptionStyleCompat.EDGE_TYPE_OUTLINE) }
    var currentEdgeColor by remember { mutableStateOf(ComposeColor.Black) }
    var currentFontPath by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "gradientTransition")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "xOffsetAnimation"
    )

    val gradientColors = listOf(
        ComposeColor(0xFF303030),
        ComposeColor(0xFF424242),
        ComposeColor(0xFF303030)
    )
    val gradientBrush = remember(xOffset) {
        Brush.horizontalGradient(
            colors = gradientColors,
            startX = 0f + (xOffset * 800),
            endX = 400f + (xOffset * 800)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(gradientBrush)
                .align(Alignment.CenterHorizontally)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    SubtitleView(ctx).apply {
                        setPadding(16, 16, 16, 16)
                        setBottomPaddingFraction(0.1f)
                        setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                        setApplyEmbeddedFontSizes(false)
                    }
                },
                update = { subtitleView ->
                    val currentStyle = CaptionStyleCompat(
                        currentForegroundColor.toArgb(),
                        currentBackgroundColor.toArgb(),
                        AndroidColor.TRANSPARENT,
                        currentEdgeType,
                        currentEdgeColor.toArgb(),
                        if (currentFontPath != null) {
                            try { Typeface.createFromAsset(context.assets, currentFontPath!!) }
                            catch (e: Exception) { Typeface.DEFAULT }
                        } else { Typeface.DEFAULT }
                    )

                    subtitleView.setStyle(currentStyle)

                    val previewCue = Cue.Builder()
                        .setText(SpannedString("This is your custom subtitle style in action."))
                        .setTextSize(20f, TypedValue.COMPLEX_UNIT_SP)
                        .build()
                    subtitleView.setCues(listOf(previewCue))
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Customize Subtitle Style:")
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                currentForegroundColor = when (currentForegroundColor) {
                    ComposeColor.White -> ComposeColor.Yellow
                    ComposeColor.Yellow -> ComposeColor.Cyan
                    else -> ComposeColor.White
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Text Color")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                currentEdgeType = when (currentEdgeType) {
                    CaptionStyleCompat.EDGE_TYPE_NONE -> CaptionStyleCompat.EDGE_TYPE_OUTLINE
                    CaptionStyleCompat.EDGE_TYPE_OUTLINE -> CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW
                    CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW -> CaptionStyleCompat.EDGE_TYPE_RAISED
                    CaptionStyleCompat.EDGE_TYPE_RAISED -> CaptionStyleCompat.EDGE_TYPE_DEPRESSED
                    else -> CaptionStyleCompat.EDGE_TYPE_NONE
                }
                currentEdgeColor = when (currentEdgeType) {
                    CaptionStyleCompat.EDGE_TYPE_OUTLINE -> ComposeColor.Red
                    CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW -> ComposeColor.Blue
                    CaptionStyleCompat.EDGE_TYPE_RAISED -> ComposeColor.Green
                    CaptionStyleCompat.EDGE_TYPE_DEPRESSED -> ComposeColor.Magenta
                    else -> ComposeColor.Black
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Edge Style/Color")
        }

        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val prefs = context.getSharedPreferences("subtitle_settings", Context.MODE_PRIVATE)
                prefs.edit()
                    .putInt("subtitle_fg_color", currentForegroundColor.toArgb())
                    .putInt("subtitle_bg_color", currentBackgroundColor.toArgb())
                    .putInt("subtitle_edge_type", currentEdgeType)
                    .putInt("subtitle_edge_color", currentEdgeColor.toArgb())
                    .apply()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Style Settings")
        }
    }
}
