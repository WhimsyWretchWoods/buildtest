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
 * The content will animate smoothly to its new scale and position using spring physics.
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
    snapToMinScaleThreshold: Float = 0.05f // New configurable threshold
): Modifier = composed {
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
            detectTransformGestures { centroid, pan, zoom, _ ->
                val currentScale = animatedScale.value
                val currentOffset = animatedOffset.value

                val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                // Calculate potential new scale
                var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                // Snapping logic based on the configurable threshold
                val shouldSnapToMinScale = (snapToMinScaleThreshold > 0f && abs(newScaleTarget - minScale) <= snapToMinScaleThreshold) || newScaleTarget < minScale

                if (shouldSnapToMinScale) {
                    newScaleTarget = minScale
                    scope.launch {
                        animatedScale.animateTo(minScale, animationSpec = springConfig)
                        animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                    }
                    return@detectTransformGestures
                }

                // --- FIX: Corrected zoom and pan logic to respect centroid ---
                // 1. Calculate the offset compensation for zoom around the centroid
                // This keeps the point under your fingers stationary during zoom.
                val zoomCompensationX = (centroid.x - (size.width / 2f + currentOffset.x)) * (1 - newScaleTarget / currentScale)
                val zoomCompensationY = (centroid.y - (size.height / 2f + currentOffset.y)) * (1 - newScaleTarget / currentScale)

                // 2. Apply the pan gesture (multiplied by current scale to feel natural at any zoom)
                // And then add the zoom compensation to get the raw target offset.
                var newOffsetXTarget = currentOffset.x + pan.x * panSensitivity * currentScale + zoomCompensationX
                var newOffsetYTarget = currentOffset.y + pan.y * panSensitivity * currentScale + zoomCompensationY
                // --- END FIX ---

                val scaledContentWidth = containerSize.width * newScaleTarget
                val scaledContentHeight = containerSize.height * newScaleTarget

                // Calculate max pan limits based on new scale and container size
                val maxPanX = max(0f, (scaledContentWidth - containerSize.width) / 2f)
                val maxPanY = max(0f, (scaledContentHeight - containerSize.height) / 2f)

                // Coerce new offset targets within calculated boundaries
                newOffsetXTarget = newOffsetXTarget.coerceIn(-maxPanX, maxPanX)
                newOffsetYTarget = newOffsetYTarget.coerceIn(-maxPanY, maxPanY)

                scope.launch {
                    animatedScale.animateTo(newScaleTarget, animationSpec = springConfig)
                    animatedOffset.animateTo(Offset(newOffsetXTarget, newOffsetYTarget), animationSpec = offsetSpringConfig)
                }
            }
        }
}
