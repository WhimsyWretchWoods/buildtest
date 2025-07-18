package test.raku.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * A custom modifier that enables pinch-to-zoom and pan gestures on the Composable it's applied to.
 * The content will directly follow the gesture and then animate smoothly to its final scale and position using spring physics when the gesture ends.
 *
 * @param zoomLimit The maximum scale factor allowed for zooming. Defaults to 5f (5 times original size).
 * @param minScale The minimum scale factor allowed. Defaults to 1f (original size).
 * @param zoomSensitivity Multiplier for the zoom gesture. Increase to make pinch-to-zoom faster.
 * @param panSensitivity Multiplier for the pan gesture. Increase to make panning faster, especially when zoomed in.
 * @param snapToMinScaleThreshold If the zoom level is within this threshold of minScale, it will snap to minScale and center.
 * Set to 0f to disable snapping to minScale. Defaults to 0.05f.
 */
fun Modifier.zoomAndPan(
    zoomLimit: Float = 5f,
    minScale: Float = 1f,
    zoomSensitivity: Float = 6f,
    panSensitivity: Float = 6f,
    snapToMinScaleThreshold: Float = 0.05f
): Modifier = composed {
    var scale by remember { mutableFloatStateOf(minScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animatedScale = remember { Animatable(minScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val springConfig = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val offsetSpringConfig = spring<Offset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scope = rememberCoroutineScope()

    LaunchedEffect(scale) {
        animatedScale.snapTo(scale)
    }
    LaunchedEffect(offset) {
        animatedOffset.snapTo(offset)
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
        .pointerInput(Unit) {
            // Use awaitPointerEventScope to detect when the gesture is truly over.
            // detectTransformGestures processes events *within* a gesture.
            // We need to react when the entire pointerInput block completes (fingers lifted).
            while (true) {
                val event = awaitPointerEventScope {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val currentScale = scale
                        val currentOffset = offset

                        val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                        var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                        val shouldSnapToMinScale = snapToMinScaleThreshold > 0f && abs(newScaleTarget - minScale) <= snapToMinScaleThreshold

                        if (shouldSnapToMinScale) {
                            newScaleTarget = minScale
                        }

                        val zoomCompensationX = (centroid.x - (containerSize.width / 2f + currentOffset.x)) * (1 - newScaleTarget / currentScale)
                        val zoomCompensationY = (centroid.y - (containerSize.height / 2f + currentOffset.y)) * (1 - newScaleTarget / currentScale)

                        var newOffsetXTarget = currentOffset.x + pan.x * panSensitivity * currentScale + zoomCompensationX
                        var newOffsetYTarget = currentOffset.y + pan.y * panSensitivity * currentScale + zoomCompensationY

                        val scaledContentWidth = containerSize.width * newScaleTarget
                        val scaledContentHeight = containerSize.height * newScaleTarget

                        val maxPanX = max(0f, (scaledContentWidth - containerSize.width) / 2f)
                        val maxPanY = max(0f, (scaledContentHeight - containerSize.height) / 2f)

                        newOffsetXTarget = newOffsetXTarget.coerceIn(-maxPanX, maxPanX)
                        newOffsetYTarget = newOffsetYTarget.coerceIn(-maxPanY, maxPanY)

                        scale = newScaleTarget
                        offset = Offset(newOffsetXTarget, newOffsetYTarget)
                    }
                }
                // This block is executed *after* detectTransformGestures completes,
                // meaning the gesture has ended (all pointers released).
                scope.launch {
                    val shouldSnapToMinScale = snapToMinScaleThreshold > 0f && abs(scale - minScale) <= snapToMinScaleThreshold

                    if (shouldSnapToMinScale) {
                        animatedScale.animateTo(minScale, animationSpec = springConfig)
                        animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                        scale = minScale
                        offset = Offset.Zero
                    } else {
                        animatedScale.animateTo(scale, animationSpec = springConfig)
                        animatedOffset.animateTo(offset, animationSpec = offsetSpringConfig)
                    }
                }
            }
        }
}
