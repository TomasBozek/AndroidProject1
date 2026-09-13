package com.example.androidproject1.core.ui.theme

import android.view.InputDevice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Which layout a screen is being asked for.
 *
 * The class follows width, but density follows *what drives the thing*: a large tablet on a wall
 * with a mouse is still denser than a handheld. Moving between classes never changes the order of
 * content, only its arrangement — someone who goes from phone to tablet finds things in the same
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
 * Layer 2 — density. One token set, two typographic densities, three layouts, four densities.
 *
 * [minTouchTarget] is a floor, not a suggestion: a smaller element must never be the only route to
 * an action.
 *
 * The fourth density is [pointer], and it is not a width. A mouse or a trackpad hits a 40 dp target
 * as reliably as a finger hits 48, so a pointer-driven device of any size gets the dense set — a
 * phone with a mouse plugged into it as much as a desktop with one. It wins over the size class,
 * because what is doing the pointing is the stronger fact.
 */
@Immutable
data class AppDensity(
    val sizeClass: SizeClass,
    /** True when a mouse or a trackpad is attached; see [pointerPresent]. */
    val pointer: Boolean,
    /** 48 dp on touch, 56 dp on a tablet, 40 dp where there is a mouse. */
    val minTouchTarget: Dp,
    val listRowHeight: Dp,
)

/**
 * The breakpoints, in one place.
 *
 * 720 and 1280. The design gives them twice and disagrees with itself — the *Prostor a velikosti*
 * table says 720 / 1280 and *Hustota* says 600 / 1000 — and this is the pair the code already had.
 */
fun sizeClassFor(widthDp: Int): SizeClass = when {
    widthDp < 720 -> SizeClass.Compact
    widthDp < 1280 -> SizeClass.Regular
    else -> SizeClass.Expanded
}

fun densityFor(sizeClass: SizeClass, pointer: Boolean = false): AppDensity = when {
    pointer -> AppDensity(sizeClass, pointer = true, minTouchTarget = 40.dp, listRowHeight = 48.dp)
    sizeClass == SizeClass.Compact ->
        AppDensity(sizeClass, pointer = false, minTouchTarget = 48.dp, listRowHeight = 56.dp)
    else ->
        AppDensity(sizeClass, pointer = false, minTouchTarget = 56.dp, listRowHeight = 56.dp)
}

/**
 * Whether anything attached to this device points.
 *
 * Read from [InputDevice] rather than from the configuration, because a mouse is plugged in and
 * unplugged while the app is running and no `Configuration` field says so on its own. It is
 * re-read when the configuration changes, which is what a device being attached produces — and
 * being wrong for a moment costs 8 dp of row height, not a broken screen.
 */
@Composable
fun pointerPresent(): Boolean {
    val configuration = LocalConfiguration.current
    return remember(configuration) { anyPointerDevice() }
}

private fun anyPointerDevice(): Boolean = InputDevice.getDeviceIds().any { id ->
    val device = InputDevice.getDevice(id) ?: return@any false
    device.supportsSource(InputDevice.SOURCE_MOUSE) ||
        device.supportsSource(InputDevice.SOURCE_TOUCHPAD)
}

internal val LocalAppDensity = staticCompositionLocalOf { densityFor(SizeClass.Compact) }
