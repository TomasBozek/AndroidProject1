package com.example.androidproject1.core.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layer 2 — elevation. Six levels and nothing between them.
 *
 * Each level has two parts: an **edge**, a hard bottom border that gives a key its body, and an
 * **ambient** shadow beneath it. Pressing shortens the edge, so the element drops by [PressTravel].
 * In dark the ambient does not read, so the edge carries the whole effect.
 *
 * The edge is drawn as a second background layer rather than through `Modifier.shadow`, because it
 * has to stay sharp: blurred, a key looks like a floating card instead of a pressed one.
 */
@Immutable
data class AppElevation(
    /** Surface, list row. */
    val level0: Dp = 0.dp,
    /** Card, tile. */
    val level1: Dp = 1.dp,
    /** Key, button. */
    val level2: Dp = 3.dp,
    /** Menu, floating action. */
    val level3: Dp = 6.dp,
    /** Dialog, sheet. */
    val level4: Dp = 10.dp,
)

/** How far a pressed element travels. */
val PressTravel: Dp = 3.dp

internal val LocalAppElevation = staticCompositionLocalOf { AppElevation() }

/**
 * A raised, pressable surface: [color] sitting on an [edge]-coloured body.
 *
 * Pass `pressed = true` and the body shrinks to a hairline, which is the travel effect — the
 * content moves down by [PressTravel] and the element looks depressed. A coloured element tints its
 * own edge; a neutral grey edge under a saturated button looks dirty, so pass the family's own
 * `edge` from [ActionColors].
 */
@Composable
fun Modifier.keySurface(
    color: Color,
    edge: Color,
    shape: Shape,
    edgeHeight: Dp = AppTheme.elevation.level2,
    pressed: Boolean = false,
): Modifier {
    val body by animateDpAsState(
        targetValue = if (pressed) edgeHeight - PressTravel else edgeHeight,
        animationSpec = tween(
            durationMillis = AppTheme.motion.pressMillis,
            easing = AppTheme.motion.pressEasing,
        ),
        label = "keySurfaceEdge",
    )
    return this
        .clip(shape)
        .background(edge)
        .padding(bottom = body.coerceAtLeast(0.dp))
        .clip(shape)
        .background(color)
}
