# Morph Animation

A minimal Android demo app that showcases a **liquid swipe transition** between screens—inspired by fluid, blob-style edges—built entirely with **Jetpack Compose**.

Swipe left or right to move between four onboarding screens. The transition uses a custom wave path that follows your finger, with spring physics and optional haptic feedback.

---

## Features

- **Liquid swipe transition** – Custom Bézier-based wave/blob edge that morphs as you drag, instead of a simple slide.
- **Bidirectional** – Swipe **left** for next, **right** for previous; direction is locked for the whole gesture so reversing mid-drag doesn’t flip the transition.
- **Finger-following wave** – The primary wave is tied to vertical finger position; additional ripple waves add depth.
- **Spring physics** – Snappy spring for release (complete or snap-back) and a low-bounce spring for completion so the wave and screen change feel in sync.
- **Velocity-aware** – Fast swipes can trigger completion even before crossing the 50% threshold.
- **Rubber-band** – Slight over-drag at 0% and 100% with a soft snap back.
- **Edge-to-edge UI** – Content under the status bar; navigation bar is hidden and can appear transiently by swiping from the bottom.
- **Four onboarding screens** – Example content with step indicator (e.g. “1 of 4”) at the top center.

---

## Tech Stack

| Category        | Choice                          |
|----------------|----------------------------------|
| Language       | Kotlin                           |
| UI             | Jetpack Compose (Material 3)     |
| Min SDK        | 24                               |
| Target SDK     | 36                               |
| Build          | Gradle (Kotlin DSL)              |

Dependencies include Compose BOM, Foundation (gestures), UI, Material3, and Animation (e.g. `Animatable`, `SpringSpec`).

---

## Requirements

- **Android Studio** (latest stable recommended)
- **JDK 11+**
- **Android device or emulator** with API 24+

---

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/MorphAnimation.git
   cd MorphAnimation
   ```

2. **Open in Android Studio**  
   Open the project folder and let Gradle sync.

3. **Run the app**  
   Select a device or emulator and run the `app` configuration (or use **Run → Run 'app'**).

No API keys or extra setup are required.

---

## Project Structure

```
app/src/main/java/com/example/morphanimation/
├── MainActivity.kt                    # Edge-to-edge, hides nav bar, sets Compose content
├── ui/
│   ├── liquid/                        # Liquid swipe engine
│   │   ├── LiquidSwipeConstants.kt   # Thresholds, spring specs, wave parameters
│   │   ├── LiquidSwipeState.kt       # Progress state, Animatable, rememberLiquidSwipeState
│   │   ├── LiquidSwipeTransition.kt  # Composable: gesture + clip + blob path (2 overloads)
│   │   ├── LiquidBlobPath.kt         # Wave path generation (right-edge & left-edge blobs)
│   │   └── SwipeDirection.kt         # NEXT / PREV enum
│   ├── onboarding/
│   │   ├── LiquidSwipeOnboarding.kt  # Wires 4 screens + LiquidSwipeTransition + index
│   │   ├── OnboardingStepIndicator.kt
│   │   └── OnboardingScreen1.kt … 4   # Individual screen composables
│   └── theme/
│       ├── Theme.kt
│       ├── Type.kt
│       └── Color.kt
```

- **Liquid:** Reusable transition and path logic; you can plug in any `screenCurrent` / `screenNext` / `screenPrev` (or the two-screen overload).
- **Onboarding:** Example usage with four screens and step indicator.
- **Theme:** Material 3 theme and typography (dynamic color on Android 12+).

---

## How It Works

### Gesture and progress

- **Drag** is handled with `detectDragGestures`. Horizontal movement is converted to a **progress** value in `[0, 1]` (and slightly beyond for rubber-band).
- **Direction** is set on first non-zero horizontal move and kept for the whole gesture (no mid-gesture flip).
- **Velocity** is derived from the last drag delta and used on release to decide “complete” vs “snap back”.

### Wave path

- **LiquidBlobPath** builds a path for the “revealed” side (next or previous screen):
  - **Right-edge blob** (swipe left → next): wave extends from the right; `progress` moves the leading edge from right to left.
  - **Left-edge blob** (swipe right → prev): same idea from the left.
- The path uses a **primary wave** (amplitude and phase depend on finger Y and constants) plus **ripple waves** (multiple frequencies and phase offsets) for a liquid look.
- **Velocity** can scale the wave bulge so faster swipes get a slightly stronger curve.

### Drawing and completion

- **Current screen** is drawn with `clipPath(path, ClipOp.Difference)` so it’s not visible under the blob.
- **Revealed screen** is drawn only inside the blob via `clipPath(path)`.
- A **shadow** along the blob edge is optional.
- **Completion:** When the user releases, if progress ≥ threshold or velocity passes a threshold, `progress` is animated to `1` with `SpringComplete`. The **index** (e.g. current onboarding step) is updated in `onReachedTarget` so the UI switches only after the wave has fully revealed the next screen. Then progress is reset to `0` for the next gesture.

---

## Using the transition in your own UI

**Bidirectional (e.g. onboarding with next + previous):**

```kotlin
val stateHolder = rememberLiquidSwipeState(onTransitionComplete = { /* optional */ })

LiquidSwipeTransition(
    stateHolder = stateHolder,
    screenCurrent = { YourCurrentScreen() },
    screenNext = { YourNextScreen() },
    screenPrev = { YourPreviousScreen() },
    onSwipeComplete = { direction ->
        when (direction) {
            SwipeDirection.NEXT -> moveToNext()
            SwipeDirection.PREV -> moveToPrev()
        }
    },
    enableHapticOnComplete = true
)
```

**Single direction (only “next”):**

```kotlin
LiquidSwipeTransition(
    stateHolder = stateHolder,
    screenA = { ScreenA() },
    screenB = { ScreenB() },
    enableHapticOnComplete = true
)
```

You keep your own index/state and update it inside `onSwipeComplete` (and in the single-direction case, you’d typically advance and then reset state so the next transition shows the next pair).

---

## Customization

Tunable values live in **`LiquidSwipeConstants.kt`**:

| Constant              | Role |
|-----------------------|------|
| `COMPLETE_THRESHOLD`  | Progress (e.g. 0.5) above which release completes the transition |
| `VELOCITY_THRESHOLD`  | Horizontal velocity (px/s) that can force completion |
| `RUBBER_BAND_MAX`     | How far progress can go below 0 or above 1 when over-dragging |
| `SpringSnap` / `SpringComplete` | Spring specs for snap-back and for completion animation |
| `CURVE_INTENSITY`, `MIN_CURVE_BULGE` | Wave bulge vs progress and width |
| `WAVE_PRIMARY_*`, `WAVE_RIPPLE_*`   | Primary and ripple wave counts, amplitudes, phase offsets |
| `WAVE_SAMPLES`        | Number of segments along the path (smoothness vs performance) |

Adjust these to change feel (bouncy vs stiff, wave size, sensitivity to velocity).

---

## License

This project is open source. Use it as you like; attribution is appreciated.

---

## Author

**Umair Bashir**  
3rd March, 2026
