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
 */
fun Modifier.zoomAndPan(
    zoomLimit: Float = 5f,
    minScale: Float = 1f,
    zoomSensitivity: Float = 6f,
    panSensitivity: Float = 6f
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
                val currentOffsetX = animatedOffset.value.x
                val currentOffsetY = animatedOffset.value.y

                val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                // Calculate potential new scale
                var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                // If zooming out to near minScale, ensure it snaps precisely to minScale
                val shouldSnapToMinScale = abs(newScaleTarget - minScale) <= 0.05f || newScaleTarget < minScale

                if (shouldSnapToMinScale) {
                    newScaleTarget = minScale
                    // When snapping to minScale, immediately set offset target to center
                    scope.launch {
                        animatedScale.animateTo(minScale, animationSpec = springConfig)
                        animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                    }
                    // Crucially, return early here if we are snapping to minScale
                    // This prevents subsequent pan/zoom calculations from interfering.
                    // return@detectTransformGestures
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

                scope.launch {
                    animatedScale.animateTo(newScaleTarget, animationSpec = springConfig)
                    animatedOffset.animateTo(Offset(newOffsetXTarget, newOffsetYTarget), animationSpec = offsetSpringConfig)
                }
            }
        }
}
