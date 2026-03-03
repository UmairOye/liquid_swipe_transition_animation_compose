/*
 * Umair Bashir
 * 3rd March, 2026
 */
package com.example.morphanimation.ui.liquid

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun liquidBlobPath(
    size: Size,
    progress: Float,
    fingerY: Float,
    velocityFactor: Float = 1f
): Path {
    val w = size.width
    val h = size.height
    if (h <= 0f) return Path()

    val curveX = when {
        progress < 0f -> w * (1f - progress * LiquidSwipeConstants.RUBBER_BAND_STIFFNESS)
        progress > 1f -> w * (1f - progress) * LiquidSwipeConstants.RUBBER_BAND_STIFFNESS
        else -> w * (1f - progress)
    }.coerceIn(0f, w)

    val waveScale = if (progress <= 0f) 0f else progress.coerceIn(0f, 1f)
    val baseAmplitude = if (waveScale <= 0f) 0f else (
        LiquidSwipeConstants.MIN_CURVE_BULGE +
        LiquidSwipeConstants.CURVE_INTENSITY * waveScale * w * velocityFactor
    )

    val primaryAmp = baseAmplitude * LiquidSwipeConstants.WAVE_PRIMARY_AMPLITUDE
    val fy = fingerY.coerceIn(0f, h)
    val twoPi = 2f * PI.toFloat()
    val kPrimary = LiquidSwipeConstants.WAVE_PRIMARY_COUNT
    val primaryPhase = (PI.toFloat() / 2f) - twoPi * kPrimary * (fy / h)

    val n = LiquidSwipeConstants.WAVE_SAMPLES
    val path = Path().apply {
        moveTo(w, 0f)
        var prevY = 0f
        var prevX = waveX(curveX, primaryAmp, 0f, h, primaryPhase)
        lineTo(prevX, 0f)
        for (i in 1..n) {
            val t = i.toFloat() / n
            val y = t * h
            val x = waveX(curveX, primaryAmp, y, h, primaryPhase)
            val dy = y - prevY
            if (dy > 0f) {
                val (dxdyPrev, dxdyCurr) = waveDerivative(primaryAmp, prevY, y, h, primaryPhase)
                val cp1x = prevX + dxdyPrev * (dy / 3f)
                val cp1y = prevY + dy / 3f
                val cp2x = x - dxdyCurr * (dy / 3f)
                val cp2y = y - dy / 3f
                cubicTo(cp1x, cp1y, cp2x, cp2y, x, y)
            }
            prevX = x
            prevY = y
        }
        lineTo(w, h)
        close()
    }
    return path
}

internal fun waveX(
    curveX: Float,
    primaryAmp: Float,
    y: Float,
    h: Float,
    primaryPhase: Float
): Float {
    val normY = y / h
    val twoPi = 2f * PI.toFloat()
    val kPrimary = LiquidSwipeConstants.WAVE_PRIMARY_COUNT
    val primary = primaryAmp * sin(twoPi * kPrimary * normY + primaryPhase)
    val rippleCounts = LiquidSwipeConstants.WAVE_RIPPLE_COUNTS
    val rippleAmps = LiquidSwipeConstants.WAVE_RIPPLE_AMPLITUDES
    val ripplePhases = LiquidSwipeConstants.WAVE_RIPPLE_PHASE_OFFSETS
    var ripples = 0f
    for (i in rippleCounts.indices) {
        val k = rippleCounts[i]
        val amp = primaryAmp * rippleAmps[i]
        ripples += amp * sin(twoPi * k * normY + ripplePhases[i])
    }
    return curveX + primary + ripples
}

private fun waveDerivative(
    primaryAmp: Float,
    prevY: Float,
    y: Float,
    h: Float,
    primaryPhase: Float
): Pair<Float, Float> {
    val twoPi = 2f * PI.toFloat()
    val invH = 1f / h
    val kPrimary = LiquidSwipeConstants.WAVE_PRIMARY_COUNT
    fun deriv(atY: Float): Float {
        val normY = atY * invH
        var d = primaryAmp * (twoPi * kPrimary * invH) * cos(twoPi * kPrimary * normY + primaryPhase)
        val rippleCounts = LiquidSwipeConstants.WAVE_RIPPLE_COUNTS
        val rippleAmps = LiquidSwipeConstants.WAVE_RIPPLE_AMPLITUDES
        val ripplePhases = LiquidSwipeConstants.WAVE_RIPPLE_PHASE_OFFSETS
        for (i in rippleCounts.indices) {
            val k = rippleCounts[i]
            val amp = primaryAmp * rippleAmps[i]
            d += amp * (twoPi * k * invH) * cos(twoPi * k * normY + ripplePhases[i])
        }
        return d
    }
    return deriv(prevY) to deriv(y)
}

fun liquidBlobPathFromLeft(
    size: Size,
    progress: Float,
    fingerY: Float,
    velocityFactor: Float = 1f
): Path {
    val w = size.width
    val h = size.height
    if (h <= 0f) return Path()

    val curveX = when {
        progress < 0f -> progress * w * LiquidSwipeConstants.RUBBER_BAND_STIFFNESS
        progress > 1f -> w + (progress - 1f) * w * LiquidSwipeConstants.RUBBER_BAND_STIFFNESS
        else -> progress * w
    }.coerceIn(0f, w)

    val waveScale = if (progress <= 0f) 0f else progress.coerceIn(0f, 1f)
    val baseAmplitude = if (waveScale <= 0f) 0f else (
        LiquidSwipeConstants.MIN_CURVE_BULGE +
        LiquidSwipeConstants.CURVE_INTENSITY * waveScale * w * velocityFactor
    )
    val primaryAmp = baseAmplitude * LiquidSwipeConstants.WAVE_PRIMARY_AMPLITUDE
    val fy = fingerY.coerceIn(0f, h)
    val twoPi = 2f * PI.toFloat()
    val kPrimary = LiquidSwipeConstants.WAVE_PRIMARY_COUNT
    val primaryPhase = (PI.toFloat() / 2f) - twoPi * kPrimary * (fy / h)

    val n = LiquidSwipeConstants.WAVE_SAMPLES
    val path = Path().apply {
        moveTo(0f, 0f)
        var prevY = 0f
        val edgeX0 = 2f * curveX - waveX(curveX, primaryAmp, 0f, h, primaryPhase)
        var prevX = edgeX0.coerceIn(0f, w)
        lineTo(prevX, 0f)
        for (i in 1..n) {
            val t = i.toFloat() / n
            val y = t * h
            val edgeX = 2f * curveX - waveX(curveX, primaryAmp, y, h, primaryPhase)
            val x = edgeX.coerceIn(0f, w)
            val dy = y - prevY
            if (dy > 0f) {
                val (dxdyPrev, dxdyCurr) = waveDerivative(primaryAmp, prevY, y, h, primaryPhase)
                val cp1x = prevX - dxdyPrev * (dy / 3f)
                val cp1y = prevY + dy / 3f
                val cp2x = x - dxdyCurr * (dy / 3f)
                val cp2y = y - dy / 3f
                cubicTo(cp1x, cp1y, cp2x, cp2y, x, y)
            }
            prevX = x
            prevY = y
        }
        lineTo(0f, h)
        close()
    }
    return path
}

fun liquidBlobFillPath(
    size: Size,
    progress: Float,
    fingerY: Float,
    velocityFactor: Float = 1f
): Path = liquidBlobPath(size, progress, fingerY, velocityFactor)
