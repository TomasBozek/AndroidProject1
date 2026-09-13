package com.example.androidproject1.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layer 2 — icon sizes. Three, and only three.
 *
 * An icon is a glyph doing a job, and the job is what picks the size: a marker inside a row is
 * [sm], a control the finger goes to is [md], a picture standing on its own is [lg]. Before this
 * there was no role at all, so every `Icon(...)` wrote whatever its call site felt like — 16, 18,
 * 22, 24, 32 — and a screen that wanted a large one had nothing to ask for, because a feature may
 * not write a `.dp`.
 *
 * The icon **set** is a separate question and is settled in D33: Material's icons stay. The design
 * names Lucide, which has no first-party Compose artifact, and the sizes are what actually differ
 * between a tidy screen and an untidy one.
 *
 * These are not touch targets. A tappable icon is wrapped by `AppIconButton`, whose box is
 * `AppTheme.density.minTouchTarget` — the icon inside it stays [md] on a phone and on a tablet.
 */
@Immutable
data class AppIcons(
    /** 18 dp — beside text in a row, in a field, or inside a small button. */
    val sm: Dp = 18.dp,
    /** 24 dp — the default, and every icon that is itself the control. */
    val md: Dp = 24.dp,
    /** 32 dp — a key, an empty state, anywhere the icon is the thing being looked at. */
    val lg: Dp = 32.dp,
)

internal val LocalAppIcons = staticCompositionLocalOf { AppIcons() }
