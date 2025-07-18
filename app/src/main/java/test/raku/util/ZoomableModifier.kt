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
    snapToMinScaleThreshold: Float = 0.05f // Defaulting to a sensible value
): Modifier = composed {
    // These now hold the *current* state directly during a gesture.
    // We use mutableStateOf for immediate UI updates, and then use Animatable for *settling* animations.
    var scale by remember { mutableFloatStateOf(minScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // These Animatable instances are now primarily for *settling* animations after a gesture ends.
    val animatedScale = remember { Animatable(minScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Your spring configs are fine for the settling animations
    val springConfig = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val offsetSpringConfig = spring<Offset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scope = rememberCoroutineScope()

    // Update the Animatable values whenever the direct state changes.
    // This ensures that if you programmatically change 'scale' or 'offset',
    // the Animatable catches up and can then animate from that point.
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
            // Apply the animated values for smooth settling
            scaleX = animatedScale.value
            scaleY = animatedScale.value
            translationX = animatedOffset.value.x
            translationY = animatedOffset.value.y
        }
        .pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { centroid, pan, zoom, _ ->
                    val currentScale = scale // Use the direct state
                    val currentOffset = offset // Use the direct state

                    val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                    // Calculate potential new scale
                    var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                    // Snapping logic - if it's going to snap, directly set the target to minScale
                    val shouldSnapToMinScale = snapToMinScaleThreshold > 0f && abs(newScaleTarget - minScale) <= snapToMinScaleThreshold

                    if (shouldSnapToMinScale) {
                        newScaleTarget = minScale
                        // Also snap offset to zero if snapping scale
                    }

                    // Calculate the offset compensation for zoom around the centroid
                    val zoomCompensationX = (centroid.x - (containerSize.width / 2f + currentOffset.x)) * (1 - newScaleTarget / currentScale)
                    val zoomCompensationY = (centroid.y - (containerSize.height / 2f + currentOffset.y)) * (1 - newScaleTarget / currentScale)

                    // Apply the pan gesture (multiplied by current scale to feel natural at any zoom)
                    var newOffsetXTarget = currentOffset.x + pan.x * panSensitivity * currentScale + zoomCompensationX
                    var newOffsetYTarget = currentOffset.y + pan.y * panSensitivity * currentScale + zoomCompensationY

                    val scaledContentWidth = containerSize.width * newScaleTarget
                    val scaledContentHeight = containerSize.height * newScaleTarget

                    // Calculate max pan limits based on new scale and container size
                    val maxPanX = max(0f, (scaledContentWidth - containerSize.width) / 2f)
                    val maxPanY = max(0f, (scaledContentHeight - containerSize.height) / 2f)

                    // Coerce new offset targets within calculated boundaries
                    newOffsetXTarget = newOffsetXTarget.coerceIn(-maxPanX, maxPanX)
                    newOffsetYTarget = newOffsetYTarget.coerceIn(-maxPanY, maxPanY)

                    // DIRECTLY UPDATE THE STATE for immediate visual feedback
                    scale = newScaleTarget
                    offset = Offset(newOffsetXTarget, newOffsetYTarget)
                },
                onGestureEnd = {
                    // Only when the gesture ends, start the settling animation
                    // This handles scenarios like releasing the zoom or pan
                    scope.launch {
                        val shouldSnapToMinScale = snapToMinScaleThreshold > 0f && abs(scale - minScale) <= snapToMinScaleThreshold

                        if (shouldSnapToMinScale) {
                            // If it's close enough, snap scale to min and offset to zero
                            animatedScale.animateTo(minScale, animationSpec = springConfig)
                            animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                            scale = minScale // Update direct state too
                            offset = Offset.Zero // Update direct state too
                        } else {
                            // Otherwise, animate to the current (potentially clamped) scale and offset
                            animatedScale.animateTo(scale, animationSpec = springConfig)
                            animatedOffset.animateTo(offset, animationSpec = offsetSpringConfig)
                        }
                    }
                }
            )
        }
}
