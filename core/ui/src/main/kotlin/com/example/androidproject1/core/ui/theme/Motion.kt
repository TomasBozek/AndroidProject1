package com.example.androidproject1.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Layer 2 — motion. Four tokens; there is no fifth.
 *
 * No animation blocks the next input: a second touch during a running animation still counts. Under
 * `prefers-reduced-motion` the travel is replaced by an immediate colour change rather than removed
 * — the operator still has to see that the press registered.
 */
@Immutable
data class AppMotion(
    /** Key travel and press feedback. */
    val pressMillis: Int = 70,
    val pressEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    /** Switch, checkbox, segmented control. */
    val toggleMillis: Int = 140,
    val toggleEasing: Easing = CubicBezierEasing(0.3f, 0f, 0.2f, 1f),
    /** Sheet and dialog: decelerate in, accelerate out. */
    val sheetEnterMillis: Int = 220,
    val sheetExitMillis: Int = 180,
    /** Screen transitions — the one place with a platform-native deviation. */
    val screenMillis: Int = 260,
)

internal val LocalAppMotion = staticCompositionLocalOf { AppMotion() }
