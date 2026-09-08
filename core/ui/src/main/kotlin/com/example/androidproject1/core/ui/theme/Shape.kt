package com.example.androidproject1.core.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layer 2 — shape. Radius is not a free parameter: each role has one, so a key, a surface and a tag
 * stay distinguishable even when everything is grey.
 *
 * A nested element's radius is smaller than its parent's by the padding between them — a 10 dp key
 * inset 12 dp inside a panel means a 22 dp panel, not a 16 dp one.
 */
@Immutable
data class AppShapes(
    /** Table, divider. */
    val none: CornerBasedShape = RoundedCornerShape(0.dp),
    /** Badge, indicator. */
    val xs: CornerBasedShape = RoundedCornerShape(4.dp),
    /** Small button, checkbox. */
    val sm: CornerBasedShape = RoundedCornerShape(8.dp),
    /** Key, field, medium button. */
    val md: CornerBasedShape = RoundedCornerShape(10.dp),
    /** Tile, large button. */
    val lg: CornerBasedShape = RoundedCornerShape(12.dp),
    /** Card, panel, dialog. */
    val xl: CornerBasedShape = RoundedCornerShape(16.dp),
    /** Bottom sheet — rounded at the top, square where it meets the edge. */
    val sheet: CornerBasedShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    /** Tag, switch, round button. */
    val pill: CornerBasedShape = RoundedCornerShape(percent = 50),
) {
    /** The radius a child should use inside a parent of [parent] radius, given [gap] of padding. */
    fun nested(parent: Dp, gap: Dp): CornerBasedShape = RoundedCornerShape((parent - gap).coerceAtLeast(0.dp))
}

/** The same roles as Material's five slots, so an unstyled `Card` or `Button` still fits. */
fun AppShapes.toShapes(): Shapes = Shapes(
    extraSmall = xs,
    small = sm,
    medium = md,
    large = lg,
    extraLarge = xl,
)

internal val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
