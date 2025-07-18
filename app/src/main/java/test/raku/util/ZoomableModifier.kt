package test.raku.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel // Explicit import for coroutineScope.cancel
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
    flingDecelerationFactor: Float = 0.005f
): Modifier = composed {
    val animatedScale = remember { Animatable(minScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val velocityState = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val springConfig = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val offsetSpringConfig = spring<Offset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scope = rememberCoroutineScope()
    var flingJob: Job? by remember { mutableStateOf(null) }

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
            awaitPointerEventScope {
                var lastPointerChange: PointerInputChange? = null
                while (true) {
                    val event = awaitPointerEvent()
                    val changes = event.changes

                    val isGestureActive = changes.any { it.pressed }

                    // Immediately cancel fling if a new gesture starts
                    if (isGestureActive && flingJob?.isActive == true) {
                        flingJob?.cancel()
                        flingJob = null
                        scope.launch { velocityState.snapTo(Offset.Zero) }
                    }

                    if (event.type == PointerEventType.Release || event.type == PointerEventType.Cancel) { // Use PointerEventType directly
                        // Gesture End - Handle Fling
                        flingJob = scope.launch {
                            if (animatedScale.value > minScale) {
                                val velocity = velocityState.value
                                if (velocity.getDistance() > 10f) {
                                    val flingAnimation = Animatable(animatedOffset.value, Offset.VectorConverter)
                                    // Calculate a target that's "far enough" for the fling physics to play out
                                    val targetOffset = animatedOffset.value + velocity * 1000f * 0.1f

                                    flingAnimation.animateTo(
                                        targetValue = calculateClampedOffset(targetOffset, animatedScale.value, containerSize),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow,
                                            visibilityThreshold = Offset.VectorConverter.convertFromVector(Offset(1f, 1f)) // Correct conversion
                                        ),
                                        initialVelocity = velocity
                                    ) {
                                        // This block runs within the animation's scope (which is a CoroutineScope)
                                        val coercedValue = calculateClampedOffset(value, animatedScale.value, containerSize)
                                        if (value != coercedValue) {
                                            this.cancel() // This 'cancel' refers to the AnimationScope.cancel(), which is correct
                                        }
                                        animatedOffset.snapTo(coercedValue) // This is a suspend function called correctly here
                                    }
                                }
                            }
                            velocityState.snapTo(Offset.Zero)
                        }
                        lastPointerChange = null
                    } else if (isGestureActive) {
                        // Using detectTransformGestures to process pan/zoom from the ongoing event
                        // This will process all active pointers and provide aggregated pan/zoom
                        // Note: The `changes` parameter passed to detectTransformGestures will be for that specific transform event,
                        // not necessarily all raw pointer changes from awaitPointerEvent.
                        detectTransformGestures { centroid, pan, zoom, transformChanges -> // Renamed `changes` to `transformChanges` for clarity
                            val currentScale = animatedScale.value
                            val currentOffset = animatedOffset.value

                            val effectiveZoom = 1f + (zoom - 1f) * zoomSensitivity

                            var newScaleTarget = (currentScale * effectiveZoom).coerceIn(minScale, zoomLimit)

                            val shouldSnapToMinScale = abs(newScaleTarget - minScale) <= 0.05f || newScaleTarget < minScale

                            if (shouldSnapToMinScale) {
                                newScaleTarget = minScale
                                scope.launch { // Launching on component's scope
                                    animatedScale.animateTo(minScale, animationSpec = springConfig)
                                    animatedOffset.animateTo(Offset.Zero, animationSpec = offsetSpringConfig)
                                }
                                return@detectTransformGestures // Exit the detectTransformGestures lambda
                            }

                            val scaledPan = pan * currentScale * panSensitivity

                            val newOffsetX = currentOffset.x + scaledPan.x + (centroid.x - currentOffset.x - size.width / 2f) * (effectiveZoom - 1f)
                            val newOffsetY = currentOffset.y + scaledPan.y + (centroid.y - currentOffset.y - size.height / 2f) * (effectiveZoom - 1f)

                            val clampedOffset = calculateClampedOffset(Offset(newOffsetX, newOffsetY), newScaleTarget, containerSize)

                            scope.launch { // Launching on component's scope
                                animatedScale.animateTo(newScaleTarget, animationSpec = springConfig)
                                animatedOffset.animateTo(clampedOffset, animationSpec = offsetSpringConfig)
                            }

                            // Update velocity for fling, using the changes from this specific transform
                            // Ensure 'transformChanges' is not empty before accessing 'first()'
                            if (transformChanges.isNotEmpty()) { // ADDED CHECK HERE
                                val firstChange = transformChanges.first()
                                if (lastPointerChange != null && firstChange.id == lastPointerChange!!.id) {
                                    val timeDelta = (firstChange.uptimeMillis - lastPointerChange!!.uptimeMillis).toFloat() / 1000f // seconds
                                    if (timeDelta > 0) {
                                        val currentDelta = firstChange.position - lastPointerChange!!.position
                                        val currentVelocity = currentDelta / timeDelta
                                        scope.launch {
                                            velocityState.snapTo(currentVelocity)
                                        }
                                    }
                                }
                                lastPointerChange = firstChange
                            }
                        }
                    }
                }
            }
        }
        .pointerInput(Unit) { // Separate pointerInput for double tap
            detectTapGestures(
                onDoubleTap = { tapOffset ->
                    flingJob?.cancel()
                    flingJob = null
                    scope.launch {
                        velocityState.snapTo(Offset.Zero)

                        val currentScale = animatedScale.value
                        val currentOffset = animatedOffset.value

                        val nextZoomLevel = doubleTapZoomLevels.firstOrNull { it > currentScale + 0.01f }
                            ?: doubleTapZoomLevels.firstOrNull() ?: minScale

                        val targetScale = nextZoomLevel.coerceIn(minScale, zoomLimit)

                        val focalPointInContentSpaceX = (tapOffset.x - size.width / 2f - currentOffset.x) / currentScale
                        val focalPointInContentSpaceY = (tapOffset.y - size.height / 2f - currentOffset.y) / currentScale

                        val targetOffsetX = (focalPointInContentSpaceX * currentScale - focalPointInContentSpaceX * targetScale) + currentOffset.x
                        val targetOffsetY = (focalPointInContentSpaceY * currentScale - focalPointInContentSpaceY * targetScale) + currentOffset.y

                        val clampedOffset = calculateClampedOffset(Offset(targetOffsetX, targetOffsetY), targetScale, containerSize)

                        animatedScale.animateTo(targetScale, animationSpec = springConfig)
                        animatedOffset.animateTo(clampedOffset, animationSpec = offsetSpringConfig)
                    }
                }
            )
        }
}
