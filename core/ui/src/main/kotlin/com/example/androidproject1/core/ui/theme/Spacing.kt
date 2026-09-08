package com.example.androidproject1.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layer 1 — the dimension scale. Base unit 4 dp, eight steps.
 *
 * A component does not reach for a number here either; it asks [Spacing] for a role. The scale is
 * `internal` for the same reason [Ramp] is.
 */
internal object Scale {
    val S1: Dp = 4.dp
    val S2: Dp = 8.dp
    val S3: Dp = 12.dp
    val S4: Dp = 16.dp
    val S6: Dp = 24.dp
    val S8: Dp = 32.dp
    val S12: Dp = 48.dp
    val S16: Dp = 64.dp
}

/** Padding inside a component — a card's own breathing room. */
@Immutable
data class Inset(
    val xs: Dp = Scale.S1,
    val sm: Dp = Scale.S2,
    val md: Dp = Scale.S3,
    val lg: Dp = Scale.S4,
    val xl: Dp = Scale.S6,
)

/** Vertical gaps: between rows, between sections. */
@Immutable
data class Stack(
    val xs: Dp = Scale.S1,
    val sm: Dp = Scale.S2,
    val md: Dp = Scale.S4,
    val lg: Dp = Scale.S6,
    val xl: Dp = Scale.S8,
)

/** Horizontal gaps: between buttons, between a label and its value. */
@Immutable
data class Inline(
    val xs: Dp = Scale.S1,
    val sm: Dp = Scale.S2,
    val md: Dp = Scale.S3,
    val lg: Dp = Scale.S4,
    val xl: Dp = Scale.S6,
)

/**
 * The spacing scale every screen measures against, so a density change is one edit rather than one
 * per screen.
 *
 * Read it through [AppTheme.spacing] rather than typing a `.dp` literal — and read the *role*, not
 * the step:
 *
 * ```
 * Column(
 *     modifier = Modifier.padding(AppTheme.spacing.inset.lg),
 *     verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
 * )
 * ```
 *
 * A literal is still right for something intrinsic to one component — a 1 dp divider, a hairline
 * border. The scale is for layout.
 */
@Immutable
data class Spacing(
    val inset: Inset = Inset(),
    val stack: Stack = Stack(),
    val inline: Inline = Inline(),
    /** Between a destructive action and the confirming one. Pressed blind, in a hurry. */
    val destructiveGap: Dp = Scale.S6,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }
