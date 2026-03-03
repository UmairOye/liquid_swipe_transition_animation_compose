/*
 * Umair Bashir
 * 3rd March, 2026
 */
package com.example.morphanimation.ui.liquid

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec

object LiquidSwipeConstants {

    const val COMPLETE_THRESHOLD = 0.5f
    const val VELOCITY_THRESHOLD = 800f
    const val RUBBER_BAND_MAX = 0.15f
    const val RUBBER_BAND_STIFFNESS = 0.3f

    val SpringSnap = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val SpringComplete = SpringSpec<Float>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow
    )

    const val CURVE_INTENSITY = 0.4f
    const val MIN_CURVE_BULGE = 24f
    const val WAVE_PRIMARY_COUNT = 2.2f
    const val WAVE_PRIMARY_AMPLITUDE = 1f
    val WAVE_RIPPLE_COUNTS = floatArrayOf(5f, 3.5f, 7f)
    val WAVE_RIPPLE_AMPLITUDES = floatArrayOf(0.22f, 0.28f, 0.18f)
    val WAVE_RIPPLE_PHASE_OFFSETS = floatArrayOf(0f, 1.2f, 2.5f)
    const val WAVE_SAMPLES = 28
}
