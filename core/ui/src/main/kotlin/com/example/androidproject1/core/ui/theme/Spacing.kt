package com.example.androidproject1.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The spacing scale every screen measures against, so a density change is one edit rather than one
 * per screen.
 *
 * Read it through [AppTheme.spacing] rather than typing a `.dp` literal:
 *
 * ```
 * Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.medium))
 * ```
 *
 * A literal is still right for something intrinsic to one component — a 1 dp divider, a hairline
 * border. The scale is for layout.
 */
@Immutable
data class Spacing(
    /** 4.dp — between tightly related items, e.g. a label and its value. */
    val extraSmall: Dp = 4.dp,
    /** 8.dp — inside a component. */
    val small: Dp = 8.dp,
    /** 16.dp — the default gap between siblings. */
    val medium: Dp = 16.dp,
    /** 24.dp — screen edge padding. */
    val large: Dp = 24.dp,
    /** 32.dp — between sections. */
    val extraLarge: Dp = 32.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }
