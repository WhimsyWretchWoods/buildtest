package test.raku.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * Enhanced zoom and pan modifier with double-tap support, momentum, and improved UX
 *
 * @param zoomLimit The maximum scale factor allowed for zooming. Defaults to 5f.
 * @param minScale The minimum scale factor allowed. Defaults to 1f.
 * @param zoomSensitivity Multiplier for the zoom gesture. Higher values make pinch-to-zoom faster.
 * @param panSensitivity Multiplier for the pan gesture. Higher values make panning faster.
 * @param doubleTapZoomLevels List of zoom levels to cycle through on double-tap. Defaults to [1f, 2f, 4f].
 * @param enableDoubleTap Whether to enable double-tap to zoom functionality.
 * @param onZoomChanged Callback invoked when zoom level changes.
 * @param onPanChanged Callback invoked when pan offset changes.
 */
fun Modifier.zoomAndPan(
    zoomLimit: Float = 5f,
    minScale: Float = 1f,
    zoomSensitivity: Float = 6f,
    panSensitivity: Float = 6f,
    doubleTapZoomLevels: List<Float> = listOf(1f, 2f, 4f),
    enableDoubleTap: Boolean = true,
    onZoomChanged: ((Float) -> Unit)? = null,
    onPanChanged: ((Offset) -> Unit)? = null
): Modifier = composed {
    val animatedScale = remember { Animatable(minScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var currentZoomLevelIndex by remember { mutableStateOf(0) }
    
    // For double-tap detection
    var lastTapTime by remember { mutableStateOf(0L) }
    var lastTapPosition by remember { mutableStateOf(Offset.Zero) }
    var tapJob by remember { mutableStateOf<Job?>(null) }

    val springConfig = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val offsetSpringConfig = spring<Offset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scope = rememberCoroutineScope()

    // Callback effects
    LaunchedEffect(animatedScale.value) {
        onZoomChanged?.invoke(animatedScale.value)
    }

    LaunchedEffect(animatedOffset.value) {
        onPanChanged?.invoke(animatedOffset.value)
    }

    /**
     * Handles double-tap zoom functionality
     */
    fun handleDoubleTap(tapPosition: Offset) {
        if (!enableDoubleTap) return

        // Find next zoom level
        currentZoomLevelIndex = (currentZoomLevelIndex + 1) % doubleTapZoomLevels.size
        val targetZoom = doubleTapZoomLevels[currentZoomLevelIndex].coerceIn(minScale, zoomLimit)

        scope.launch {
            if (targetZoom == minScale) {
                // Zoom to fit - center the content
                animatedScale.animateTo(targetZoom, animationSpec = springConfig)
                animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
            } else {
                // Zoom to specific level, focusing on tap position
                val currentScale = animatedScale.value
                val currentOffset = animatedOffset.value
                
                // Calculate focal point relative to content
                val focalPointX = (tapPosition.x - containerSize.width / 2f) / currentScale - currentOffset.x
                val focalPointY = (tapPosition.y - containerSize.height / 2f) / currentScale - currentOffset.y
                
                // Calculate new offset to keep focal point in same screen position
                val newOffsetX = currentOffset.x + (focalPointX * (currentScale * (targetZoom / currentScale) - currentScale))
                val newOffsetY = currentOffset.y + (focalPointY * (currentScale * (targetZoom / currentScale) - currentScale))
                
                // Apply boundary constraints
                val scaledContentWidth = containerSize.width * targetZoom
                val scaledContentHeight = containerSize.height * targetZoom
                val maxPanX = max(0f, (scaledContentWidth - containerSize.width) / 2f)
                val maxPanY = max(0f, (scaledContentHeight - containerSize.height) / 2f)
                
                val constrainedOffset = Offset(
                    newOffsetX.coerceIn(-maxPanX, maxPanX),
                    newOffsetY.coerceIn(-maxPanY, maxPanY)
                )
                
                animatedScale.animateTo(targetZoom, animationSpec = springConfig)
                animatedOffset.animateTo(constrainedOffset, animationSpec = offsetSpringConfig)
            }
        }
    }

    /**
     * Detects double-tap gestures
     */
    fun handleTap(tapPosition: Offset) {
        val currentTime = System.currentTimeMillis()
        val tapThreshold = 300L // milliseconds
        val positionThreshold = 50f // pixels
        
        if (currentTime - lastTapTime < tapThreshold && 
            (tapPosition - lastTapPosition).getDistance() < positionThreshold) {
            // Double tap detected
            tapJob?.cancel()
            handleDoubleTap(tapPosition)
        } else {
            // Single tap - start timer for potential double tap
            tapJob?.cancel()
            tapJob = scope.launch {
                delay(tapThreshold)
                // Single tap confirmed (no double tap occurred)
                // Could add single tap handling here if needed
            }
        }
        
        lastTapTime = currentTime
        lastTapPosition = tapPosition
    }

    this
        .onSizeChanged { size ->
            containerSize = size
        }
        .graphicsLayer {
            scaleX = animatedScale.value
            scaleY = animatedScale.value
            translationX = animatedOffset.value.x
            translationY = animatedOffset.value.y
        }
        .pointerInput(enableDoubleTap) {
            if (enableDoubleTap) {
                detectTapGestures(
                    onTap = { offset ->
                        handleTap(offset)
                    }
                )
            }
        }
        .pointerInput(Unit) {
            detectTransformGestures { centroid, pan, zoom, _ ->
                // Cancel any pending tap jobs when transform gestures start
                tapJob?.cancel()
                
                val currentScale = animatedScale.value
                val currentOffsetX = animatedOffset.value.x
                val currentOffsetY = animatedOffset.value.y

                val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                // Calculate potential new scale
                var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                // If zooming out to near minScale, ensure it snaps precisely to minScale
                val shouldSnapToMinScale = abs(newScaleTarget - minScale) <= 0.05f || newScaleTarget < minScale

                if (shouldSnapToMinScale) {
                    newScaleTarget = minScale
                    currentZoomLevelIndex = 0 // Reset to first zoom level
                    
                    // When snapping to minScale, immediately set offset target to center
                    scope.launch {
                        animatedScale.animateTo(minScale, animationSpec = springConfig)
                        animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                    }
                    return@detectTransformGestures
                }

                // Normal zoom and pan logic when not at minScale
                val focalPointX = (centroid.x - size.width / 2f) / currentScale - currentOffsetX
                val focalPointY = (centroid.y - size.height / 2f) / currentScale - currentOffsetY

                val effectivePan = pan * panSensitivity

                var newOffsetXTarget = currentOffsetX + effectivePan.x * currentScale + (focalPointX * (currentScale * effectiveZoom - currentScale))
                var newOffsetYTarget = currentOffsetY + effectivePan.y * currentScale + (focalPointY * (currentScale * effectiveZoom - currentScale))

                val scaledContentWidth = containerSize.width * newScaleTarget
                val scaledContentHeight = containerSize.height * newScaleTarget

                val maxPanX = max(0f, (scaledContentWidth - containerSize.width) / 2f)
                val maxPanY = max(0f, (scaledContentHeight - containerSize.height) / 2f)

                newOffsetXTarget = newOffsetXTarget.coerceIn(-maxPanX, maxPanX)
                newOffsetYTarget = newOffsetYTarget.coerceIn(-maxPanY, maxPanY)

                // Update current zoom level index based on current scale
                val closestZoomIndex = doubleTapZoomLevels.indexOfFirst { level ->
                    abs(newScaleTarget - level) < 0.1f
                }
                if (closestZoomIndex != -1) {
                    currentZoomLevelIndex = closestZoomIndex
                }

                scope.launch {
                    animatedScale.animateTo(newScaleTarget, animationSpec = springConfig)
                    animatedOffset.animateTo(Offset(newOffsetXTarget, newOffsetYTarget), animationSpec = offsetSpringConfig)
                }
            }
        }
}

/**
 * Extension function to reset zoom and pan to initial state
 */
fun Modifier.resetZoomAndPan(
    resetTrigger: Boolean,
    minScale: Float = 1f,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = spring()
): Modifier = composed {
    val animatedScale = remember { Animatable(minScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(resetTrigger) {
        if (resetTrigger) {
            scope.launch {
                animatedScale.animateTo(minScale, animationSpec = animationSpec)
                animatedOffset.animateTo(Offset.Zero, animationSpec = spring())
            }
        }
    }

    this
}
