package com.example.androidproject1.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Which layout a screen is being asked for.
 *
 * The class follows width, but density follows *what drives the thing*: a large tablet on a wall
 * with a mouse is still denser than a handheld. Moving between classes never changes the order of
 * content, only its arrangement — someone who goes from phone to till finds things in the same
 * place in the flow.
 */
enum class SizeClass {
    /** Under 720 dp. One layer of content, navigation at the bottom, overlays as sheets. */
    Compact,

    /** 720–1279 dp. A split layout with a navigation rail; overlays as centred dialogs. */
    Regular,

    /** 1280 dp and up. Three permanent regions, nothing hidden. */
    Expanded,
}

/**
 * Layer 2 — density. One token set, two typographic densities, three layouts.
 *
 * [minTouchTarget] is a floor, not a suggestion: a smaller element must never be the only route to
 * an action.
 */
@Immutable
data class AppDensity(
    val sizeClass: SizeClass,
    /** 48 dp on touch, 56 dp on a till. 40 dp exists, but only where there is a mouse. */
    val minTouchTarget: Dp,
    val listRowHeight: Dp,
)

/** The breakpoints, in one place. */
fun sizeClassFor(widthDp: Int): SizeClass = when {
    widthDp < 720 -> SizeClass.Compact
    widthDp < 1280 -> SizeClass.Regular
    else -> SizeClass.Expanded
}

fun densityFor(sizeClass: SizeClass): AppDensity = when (sizeClass) {
    SizeClass.Compact -> AppDensity(sizeClass, minTouchTarget = 48.dp, listRowHeight = 56.dp)
    SizeClass.Regular -> AppDensity(sizeClass, minTouchTarget = 56.dp, listRowHeight = 56.dp)
    SizeClass.Expanded -> AppDensity(sizeClass, minTouchTarget = 56.dp, listRowHeight = 56.dp)
}

internal val LocalAppDensity = staticCompositionLocalOf { densityFor(SizeClass.Compact) }
