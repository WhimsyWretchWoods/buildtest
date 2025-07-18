package test.raku.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.cancel
import kotlin.math.abs
import kotlin.math.max

/**
 * A custom modifier that enables pinch-to-zoom and pan gestures on the Composable it's applied to.
 * Includes double-tap zoom and momentum-based panning.
 *
 * @param zoomLimit The maximum scale factor allowed for zooming. Defaults to 5f (5 times original size).
 * @param minScale The minimum scale factor allowed. Defaults to 1f (original size).
 * @param zoomSensitivity Multiplier for the zoom gesture. Increase to make pinch-to-zoom faster.
 * @param panSensitivity Multiplier for the pan gesture. Increase to make panning faster, especially when zoomed in.
 * @param doubleTapZoomLevels A list of zoom levels to cycle through on double-tap. Defaults to [1f, 2f, 4f].
 * @param flingDecelerationFactor Factor to control how quickly fling animation decelerates. Higher value means faster stop.
 */
fun Modifier.zoomAndPan(
    zoomLimit: Float = 5f,
    minScale: Float = 1f,
    zoomSensitivity: Float = 6f,
    panSensitivity: Float = 6f,
    doubleTapZoomLevels: List<Float> = listOf(1f, 2f, 4f),
    flingDecelerationFactor: Float = 0.005f // Adjusted for a reasonable default
): Modifier = composed {
    val animatedScale = remember { Animatable(minScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // State to hold the current velocity for fling
    val velocityState = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val springConfig = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val offsetSpringConfig = spring<Offset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val flingSpringConfig = spring<Offset>(
        dampingRatio = Spring.DampingRatioNoBouncy, // Fling should decelerate, not bounce
        stiffness = Spring.StiffnessLow // Slower deceleration for a smoother fling end
    )


    val scope = rememberCoroutineScope()

    // Helper function to calculate and coerce offset
    fun calculateClampedOffset(
        currentOffset: Offset,
        newScale: Float,
        containerSize: IntSize
    ): Offset {
        val scaledContentWidth = containerSize.width * newScale
        val scaledContentHeight = containerSize.height * newScale

        val maxPanX = max(0f, (scaledContentWidth - containerSize.width) / 2f)
        val maxPanY = max(0f, (scaledContentHeight - containerSize.height) / 2f)

        return Offset(
            currentOffset.x.coerceIn(-maxPanX, maxPanX),
            currentOffset.y.coerceIn(-maxPanY, maxPanY)
        )
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
            // Cancel any ongoing fling animation when new gesture starts
            detectTransformGestures(onGestureStart = {
                scope.launch { velocityState.snapTo(Offset.Zero) } // Stop fling
            }) { centroid, pan, zoom, _ ->
                val currentScale = animatedScale.value
                val currentOffset = animatedOffset.value

                val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                val shouldSnapToMinScale = abs(newScaleTarget - minScale) <= 0.05f || newScaleTarget < minScale

                if (shouldSnapToMinScale) {
                    newScaleTarget = minScale
                    scope.launch {
                        animatedScale.animateTo(minScale, animationSpec = springConfig)
                        animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                    }
                    return@detectTransformGestures
                }

                // Normal zoom and pan logic when not at minScale
                // Adjust pan based on current scale, so a small pan moves less content when zoomed out.
                val scaledPan = pan * currentScale * panSensitivity

                // Calculate offset considering focal point
                val newOffsetX = currentOffset.x + scaledPan.x + (centroid.x - currentOffset.x - size.width / 2f) * (effectiveZoom - 1f)
                val newOffsetY = currentOffset.y + scaledPan.y + (centroid.y - currentOffset.y - size.height / 2f) * (effectiveZoom - 1f)

                val clampedOffset = calculateClampedOffset(Offset(newOffsetX, newOffsetY), newScaleTarget, containerSize)

                scope.launch {
                    animatedScale.animateTo(newScaleTarget, animationSpec = springConfig)
                    animatedOffset.animateTo(clampedOffset, animationSpec = offsetSpringConfig)
                }
            }
        }
        .pointerInput(Unit) { // Separate pointerInput for double tap
            detectTapGestures(
                onDoubleTap = { tapOffset ->
                    scope.launch {
                        // Stop any ongoing fling
                        velocityState.snapTo(Offset.Zero)

                        val currentScale = animatedScale.value
                        val currentOffset = animatedOffset.value

                        // Find the next target zoom level
                        val nextZoomLevel = doubleTapZoomLevels.firstOrNull { it > currentScale + 0.01f }
                            ?: doubleTapZoomLevels.firstOrNull() ?: minScale

                        val targetScale = nextZoomLevel.coerceIn(minScale, zoomLimit)

                        // Calculate new offset for double-tap zoom to focal point
                        // Formula: new_offset = old_offset + (focal_point - old_offset) * (1 - new_scale / old_scale)
                        val zoomFactor = targetScale / currentScale
                        val focalPointInContentSpaceX = (tapOffset.x - size.width / 2f - currentOffset.x) / currentScale
                        val focalPointInContentSpaceY = (tapOffset.y - size.height / 2f - currentOffset.y) / currentScale

                        val targetOffsetX = (focalPointInContentSpaceX * currentScale - focalPointInContentSpaceX * targetScale) + currentOffset.x
                        val targetOffsetY = (focalPointInContentSpaceY * currentScale - focalPointInContentSpaceY * targetScale) + currentOffset.y

                        val clampedOffset = calculateClampedOffset(Offset(targetOffsetX, targetOffsetY), targetScale, containerSize)


                        // Animate to new scale and offset
                        animatedScale.animateTo(targetScale, animationSpec = springConfig)
                        animatedOffset.animateTo(clampedOffset, animationSpec = offsetSpringConfig)
                    }
                }
            )
        }
        .pointerInput(Unit) { // Separate pointerInput for fling detection
            var lastPosition: Offset? = null
            detectTransformGestures(
                onGestureEnd = {
                    scope.launch {
                        if (animatedScale.value > minScale) { // Only fling if zoomed in
                            val velocity = velocityState.value
                            if (velocity.getDistance() > 10f) { // Only fling if sufficient velocity
                                var currentOffset = animatedOffset.value
                                var currentVelocity = velocity

                                // Continuously animate until velocity is near zero or boundary hit
                                while (currentVelocity.getDistance() > 1f) { // Stop when velocity is very small
                                    currentOffset += currentVelocity * flingDecelerationFactor

                                    val clampedOffset = calculateClampedOffset(currentOffset, animatedScale.value, containerSize)

                                    // If we hit a boundary, reduce velocity in that direction
                                    if (clampedOffset.x == currentOffset.x && clampedOffset.y == currentOffset.y) {
                                        // Still moving freely
                                        currentVelocity *= 0.95f // Simple decay for continuous movement
                                    } else {
                                        // Hit boundary, snap to boundary and reduce velocity
                                        animatedOffset.snapTo(clampedOffset)
                                        currentVelocity = Offset.Zero // Stop fling if boundary hit
                                        break // Exit loop if hitting a boundary
                                    }

                                    animatedOffset.animateTo(clampedOffset, animationSpec = tween(durationMillis = 16)) // Animate frame by frame
                                    // Add a small delay to simulate discrete steps
                                    // kotlinx.coroutines.delay(16) // If you need a more explicit frame-by-frame animation
                                }
                            }
                        }
                        velocityState.snapTo(Offset.Zero) // Ensure velocity is reset
                    }
                }
            ) { _, _, _, rawChange ->
                // Calculate velocity from raw change
                val previousPosition = lastPosition
                lastPosition = rawChange.position
                if (previousPosition != null) {
                    val delta = rawChange.position - previousPosition
                    val timeDelta = rawChange.uptimeMillis - rawChange.previousUptimeMillis
                    if (timeDelta > 0) {
                        val newVelocity = delta / timeDelta.toFloat()
                        scope.launch {
                            // Smoothly update velocity for fling calculation
                            velocityState.animateTo(newVelocity * 1000f, animationSpec = spring(stiffness = Spring.StiffnessHigh)) // Convert to px/sec
                        }
                    }
                }
            }
        }
}
