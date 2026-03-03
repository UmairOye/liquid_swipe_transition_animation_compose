/*
 * Umair Bashir
 * 3rd March, 2026
 */
package com.example.morphanimation.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class LiquidSwipeState(
    val progress: Float,
    val isDragging: Boolean,
    val fingerY: Float
)

class LiquidSwipeStateHolder(
    val progressAnimatable: Animatable<Float, AnimationVector1D>,
    val scope: CoroutineScope,
    private val onTransitionComplete: () -> Unit
) {
    val progress: Float get() = progressAnimatable.value

    suspend fun snapTo(value: Float) {
        progressAnimatable.snapTo(value)
    }

    suspend fun animateTo(
        value: Float,
        spec: AnimationSpec<Float> = LiquidSwipeConstants.SpringSnap,
        onReachedTarget: () -> Unit = {}
    ) {
        progressAnimatable.animateTo(value, spec)
        if (value >= 1f) {
            onReachedTarget()
            onTransitionComplete()
            progressAnimatable.snapTo(0f)
        }
    }
}

@Composable
fun rememberLiquidSwipeState(
    onTransitionComplete: () -> Unit = {}
): LiquidSwipeStateHolder {
    val scope = rememberCoroutineScope()
    val progressAnimatable = remember { Animatable(0f) }
    val callbackRef = remember { mutableStateOf(onTransitionComplete) }
    callbackRef.value = onTransitionComplete
    return remember(progressAnimatable, scope) {
        LiquidSwipeStateHolder(progressAnimatable, scope) { callbackRef.value() }
    }
}
