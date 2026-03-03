/*
 * Umair Bashir
 * 3rd March, 2026
 */
package com.example.morphanimation.ui.liquid

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun LiquidSwipeTransition(
    stateHolder: LiquidSwipeStateHolder,
    modifier: Modifier = Modifier,
    screenCurrent: @Composable () -> Unit,
    screenNext: @Composable () -> Unit,
    screenPrev: @Composable () -> Unit,
    onSwipeComplete: (SwipeDirection) -> Unit,
    enableHapticOnComplete: Boolean = true,
    enableBlurBehindBlob: Boolean = false
) {
    var dragDirection by remember { mutableStateOf<Boolean?>(null) }
    val view = LocalView.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    var fingerY by remember { mutableFloatStateOf(0f) }
    var velocityFactor by remember { mutableFloatStateOf(1f) }
    val progress = stateHolder.progressAnimatable.value
    val sizeF = Size(size.width.toFloat(), size.height.toFloat())

    val path = remember(progress, fingerY, size, velocityFactor, dragDirection) {
        when (dragDirection) {
            true -> liquidBlobPath(sizeF, progress, fingerY, velocityFactor)
            false -> liquidBlobPathFromLeft(sizeF, progress, fingerY, velocityFactor)
            null -> liquidBlobPath(sizeF, 0f, fingerY, velocityFactor)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .liquidSwipeGestureBidirectional(
                size = size,
                progress = progress,
                dragDirection = dragDirection,
                onDragDirection = { dragDirection = it },
                onProgressDelta = { delta, fy, _ ->
                    fingerY = fy
                    stateHolder.scope.launch {
                        val newProgress = (stateHolder.progress + delta).coerceIn(
                            -LiquidSwipeConstants.RUBBER_BAND_MAX,
                            1f + LiquidSwipeConstants.RUBBER_BAND_MAX
                        )
                        stateHolder.snapTo(newProgress)
                    }
                },
                onDragStart = {
                    fingerY = it
                },
                onDragEnd = { velocityPxPerSec ->
                    val velThreshold = LiquidSwipeConstants.VELOCITY_THRESHOLD
                    val threshold = LiquidSwipeConstants.COMPLETE_THRESHOLD
                    val goingNext = (dragDirection == true)
                    val goingPrev = (dragDirection == false)
                    val shouldComplete = when {
                        goingNext -> stateHolder.progress >= threshold || velocityPxPerSec < -velThreshold
                        goingPrev -> stateHolder.progress >= threshold || velocityPxPerSec > velThreshold
                        else -> false
                    }
                    stateHolder.scope.launch {
                        if (shouldComplete) {
                            val direction = if (goingNext) SwipeDirection.NEXT else SwipeDirection.PREV
                            stateHolder.animateTo(
                                1f,
                                LiquidSwipeConstants.SpringComplete,
                                onReachedTarget = {
                                    onSwipeComplete(direction)
                                    if (enableHapticOnComplete) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            )
                            dragDirection = null
                        } else {
                            stateHolder.animateTo(0f, LiquidSwipeConstants.SpringSnap)
                            dragDirection = null
                        }
                    }
                }
            )
    ) {
        Box(
            Modifier.fillMaxSize()
                .drawWithContent {
                    clipPath(path, ClipOp.Difference) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            screenCurrent()
        }

        Box(Modifier.fillMaxSize().drawWithLiquidClip(path, enableBlurBehindBlob)) {
            when (dragDirection) {
                true -> screenNext()
                false -> screenPrev()
                null -> { }
            }
        }

        if (progress in 0.001f..0.999f && dragDirection != null) {
            Canvas(Modifier.fillMaxSize()) {
                val p = when (dragDirection) {
                    true -> liquidBlobPath(sizeF, progress, fingerY, velocityFactor)
                    false -> liquidBlobPathFromLeft(sizeF, progress, fingerY, velocityFactor)
                    null -> Path()
                }
                drawPath(p, color = Color.Black.copy(alpha = 0.06f))
            }
        }
    }
}

@Composable
fun LiquidSwipeTransition(
    stateHolder: LiquidSwipeStateHolder,
    modifier: Modifier = Modifier,
    screenA: @Composable () -> Unit,
    screenB: @Composable () -> Unit,
    enableHapticOnComplete: Boolean = true,
    enableBlurBehindBlob: Boolean = false
) {
    val view = LocalView.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var fingerY by remember { mutableFloatStateOf(0f) }
    var velocityFactor by remember { mutableFloatStateOf(1f) }

    val progress = stateHolder.progressAnimatable.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .liquidSwipeGesture(
                size = size,
                progress = progress,
                onProgressDelta = { delta, fy, velocity ->
                    fingerY = fy
                    velocityFactor = 1f + (abs(velocity) / 2000f).coerceIn(0f, 0.5f)
                    stateHolder.scope.launch {
                        val newProgress = (stateHolder.progress + delta).coerceIn(
                            -LiquidSwipeConstants.RUBBER_BAND_MAX,
                            1f + LiquidSwipeConstants.RUBBER_BAND_MAX
                        )
                        stateHolder.snapTo(newProgress)
                    }
                },
                onDragStart = {
                    isDragging = true
                    fingerY = it
                },
                onDragEnd = { velocityPxPerSec ->
                    isDragging = false
                    val velThreshold = LiquidSwipeConstants.VELOCITY_THRESHOLD

                    stateHolder.scope.launch {
                        val currentProgress = stateHolder.progressAnimatable.value
                        val threshold = LiquidSwipeConstants.COMPLETE_THRESHOLD
                        val shouldComplete = currentProgress >= threshold ||
                            (velocityPxPerSec < -velThreshold)
                        if (shouldComplete) {
                            stateHolder.animateTo(1f, LiquidSwipeConstants.SpringComplete)
                            if (enableHapticOnComplete) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        } else {
                            stateHolder.animateTo(0f, LiquidSwipeConstants.SpringSnap)
                        }
                    }
                }
            )
    ) {
        val sizeF = Size(size.width.toFloat(), size.height.toFloat())
        val path = remember(progress, fingerY, size, velocityFactor) {
            liquidBlobPath(sizeF, progress, fingerY, velocityFactor)
        }

        Box(
            Modifier.fillMaxSize()
                .drawWithContent {
                    clipPath(path, ClipOp.Difference) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            screenA()
        }

        Box(
            Modifier.fillMaxSize()
                .drawWithLiquidClip(path, enableBlur = enableBlurBehindBlob)
        ) {
            screenB()
        }

        if (progress in 0.001f..0.999f) {
            Canvas(Modifier.fillMaxSize()) {
                val shadowPath = liquidBlobPath(sizeF, progress, fingerY, velocityFactor)
                drawPath(
                    shadowPath,
                    color = Color.Black.copy(alpha = 0.06f)
                )
            }
        }
    }
}

private fun Modifier.liquidSwipeGestureBidirectional(
    size: IntSize,
    progress: Float,
    dragDirection: Boolean?,
    onDragDirection: (Boolean) -> Unit,
    onProgressDelta: (deltaProgress: Float, fingerY: Float, velocityPxPerSec: Float) -> Unit,
    onDragStart: (fingerY: Float) -> Unit,
    onDragEnd: (velocityPxPerSec: Float) -> Unit
): Modifier = this.then(
    Modifier.pointerInput(size) {
        val width = size.width.toFloat()
        if (width <= 0f) return@pointerInput
        var prevX = 0f
        var lastX = 0f
        var prevTime = 0L
        var lastTime = 0L
        var lockedDirection: Boolean? = null

        detectDragGestures(
            onDragStart = { offset ->
                lastX = offset.x
                lastTime = System.currentTimeMillis()
                lockedDirection = null
                onDragStart(offset.y)
            },
            onDrag = { change, dragAmount ->
                if (lockedDirection == null && dragAmount.x != 0f) {
                    lockedDirection = (dragAmount.x < 0f)
                    onDragDirection(lockedDirection!!)
                }
                prevX = lastX
                prevTime = lastTime
                lastX = change.position.x
                lastTime = System.currentTimeMillis()
                val dx = dragAmount.x
                val deltaProgress = when (lockedDirection) {
                    true -> -dx / width
                    false -> dx / width
                    null -> 0f
                }
                onProgressDelta(deltaProgress, change.position.y, 0f)
            },
            onDragEnd = {
                val dt = (lastTime - prevTime).coerceAtLeast(1L)
                val velocityPxPerSec = (lastX - prevX) / dt * 1000f
                onDragEnd(velocityPxPerSec)
            }
        )
    }
)

private fun Modifier.liquidSwipeGesture(
    size: IntSize,
    progress: Float,
    onProgressDelta: (deltaProgress: Float, fingerY: Float, velocityPxPerSec: Float) -> Unit,
    onDragStart: (fingerY: Float) -> Unit,
    onDragEnd: (velocityPxPerSec: Float) -> Unit
): Modifier = this.then(
    Modifier.pointerInput(size) {
        val width = size.width.toFloat()
        if (width <= 0f) return@pointerInput

        var prevX = 0f
        var lastX = 0f
        var prevTime = 0L
        var lastTime = 0L

        detectDragGestures(
            onDragStart = { offset ->
                lastX = offset.x
                lastTime = System.currentTimeMillis()
                onDragStart(offset.y)
            },
            onDrag = { change, dragAmount ->
                prevX = lastX
                prevTime = lastTime
                lastX = change.position.x
                lastTime = System.currentTimeMillis()
                val dx = dragAmount.x
                val deltaProgress = -dx / width
                onProgressDelta(deltaProgress, change.position.y, 0f)
            },
            onDragEnd = {
                val dt = (lastTime - prevTime).coerceAtLeast(1L)
                val velocityPxPerSec = (lastX - prevX) / dt * 1000f
                onDragEnd(velocityPxPerSec)
            }
        )
    }
)

private fun Modifier.drawWithLiquidClip(
    path: Path,
    enableBlur: Boolean
): Modifier = this.then(
    Modifier.drawWithContent {
        clipPath(path) {
            this@drawWithContent.drawContent()
        }
    }
)
