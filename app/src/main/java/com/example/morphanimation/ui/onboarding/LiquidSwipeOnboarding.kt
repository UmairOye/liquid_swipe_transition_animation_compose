/*
 * Umair Bashir
 * 3rd March, 2026
 */
package com.example.morphanimation.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.morphanimation.ui.liquid.LiquidSwipeTransition
import com.example.morphanimation.ui.liquid.SwipeDirection
import com.example.morphanimation.ui.liquid.rememberLiquidSwipeState

private const val TOTAL_ONBOARDING_SCREENS = 4

@Composable
fun LiquidSwipeOnboarding() {
    var currentIndex by remember { mutableStateOf(0) }
    val stateHolder = rememberLiquidSwipeState(onTransitionComplete = {})

    Box(modifier = Modifier.fillMaxSize()) {
        LiquidSwipeTransition(
            stateHolder = stateHolder,
            modifier = Modifier.fillMaxSize(),
            screenCurrent = { OnboardingScreenByIndex(currentIndex) },
            screenNext = { OnboardingScreenByIndex((currentIndex + 1) % TOTAL_ONBOARDING_SCREENS) },
            screenPrev = { OnboardingScreenByIndex((currentIndex - 1 + TOTAL_ONBOARDING_SCREENS) % TOTAL_ONBOARDING_SCREENS) },
            onSwipeComplete = { direction ->
                currentIndex = when (direction) {
                    SwipeDirection.NEXT -> (currentIndex + 1) % TOTAL_ONBOARDING_SCREENS
                    SwipeDirection.PREV -> (currentIndex - 1 + TOTAL_ONBOARDING_SCREENS) % TOTAL_ONBOARDING_SCREENS
                }
            },
            enableHapticOnComplete = true
        )
    }
}

@Composable
private fun OnboardingScreenByIndex(index: Int) {
    when (index) {
        0 -> OnboardingScreen1(step = 0, total = TOTAL_ONBOARDING_SCREENS)
        1 -> OnboardingScreen2(step = 1, total = TOTAL_ONBOARDING_SCREENS)
        2 -> OnboardingScreen3(step = 2, total = TOTAL_ONBOARDING_SCREENS)
        else -> OnboardingScreen4(step = 3, total = TOTAL_ONBOARDING_SCREENS)
    }
}
