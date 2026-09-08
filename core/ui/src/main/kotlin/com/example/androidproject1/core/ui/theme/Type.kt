package com.example.androidproject1.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * The one family the whole system uses.
 *
 * The scale below is defined in roles rather than in sizes, so swapping this for a brand face is a
 * single edit and no screen changes. Register the face in `res/font/` and return a [FontFamily]
 * built from it; the weights the scale asks for are 400, 600, 700 and 800.
 */
val AppFontFamily: FontFamily = FontFamily.Default

/** Every monetary and quantity value opts into tabular figures, so columns of digits line up. */
private const val TABULAR = "tnum"

/**
 * Layer 2 — typography. Nine roles, one family, two densities.
 *
 * A component asks for `AppTheme.typography.titleMd` and gets the right size for the device it is
 * running on: [regular] is [compact] scaled by 1.15, and the line-height ratio is held, so what
 * changes between the two is size, not rhythm.
 */
@Immutable
data class AppTypography(
    /** The amount on a display; change due. */
    val displayXl: TextStyle,
    /** Screen name, total. */
    val displayLg: TextStyle,
    /** Dialog title. */
    val titleLg: TextStyle,
    /** Section, category, tile. */
    val titleMd: TextStyle,
    /** Running text, descriptions. */
    val bodyLg: TextStyle,
    /** List rows — the default. */
    val bodyMd: TextStyle,
    /** Field labels, small buttons. */
    val labelMd: TextStyle,
    /** Group headings, in caps. */
    val labelSm: TextStyle,
    /** Prices and quantities. */
    val numericMd: TextStyle,
)

private fun style(
    size: TextUnit,
    lineHeight: TextUnit,
    weight: FontWeight,
    letterSpacing: TextUnit = 0.sp,
    tabular: Boolean = false,
) = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = size,
    lineHeight = lineHeight,
    fontWeight = weight,
    letterSpacing = letterSpacing,
    fontFeatureSettings = if (tabular) TABULAR else null,
)

/** Phone, small tablet, handheld terminal — the primary class. */
fun compactTypography(): AppTypography = AppTypography(
    displayXl = style(40.sp, 42.sp, FontWeight.ExtraBold, tabular = true),
    displayLg = style(32.sp, 36.sp, FontWeight.ExtraBold),
    titleLg = style(24.sp, 29.sp, FontWeight.Bold),
    titleMd = style(20.sp, 25.sp, FontWeight.Bold),
    bodyLg = style(17.sp, 25.sp, FontWeight.Normal),
    bodyMd = style(15.sp, 22.sp, FontWeight.Normal),
    labelMd = style(13.sp, 17.sp, FontWeight.SemiBold),
    labelSm = style(11.5.sp, 15.sp, FontWeight.Bold, letterSpacing = 0.06.em),
    numericMd = style(17.sp, 22.sp, FontWeight.Bold, tabular = true),
)

/** Tablet, till, monitor. The compact scale × 1.15. */
fun regularTypography(): AppTypography = AppTypography(
    displayXl = style(46.sp, 48.sp, FontWeight.ExtraBold, tabular = true),
    displayLg = style(37.sp, 41.sp, FontWeight.ExtraBold),
    titleLg = style(28.sp, 33.sp, FontWeight.Bold),
    titleMd = style(23.sp, 28.sp, FontWeight.Bold),
    bodyLg = style(19.sp, 28.sp, FontWeight.Normal),
    bodyMd = style(17.sp, 25.sp, FontWeight.Normal),
    labelMd = style(15.sp, 20.sp, FontWeight.SemiBold),
    labelSm = style(13.sp, 17.sp, FontWeight.Bold, letterSpacing = 0.06.em),
    numericMd = style(19.sp, 25.sp, FontWeight.Bold, tabular = true),
)

/**
 * The same roles spelled as Material's scale, so a `Text` that names no style still lands on this
 * typography. Material has fifteen slots for nine roles; the extras double up rather than invent a
 * tenth size.
 */
fun AppTypography.toTypography(): Typography = Typography(
    displayLarge = displayXl,
    displayMedium = displayXl,
    displaySmall = displayLg,
    headlineLarge = displayLg,
    headlineMedium = titleLg,
    headlineSmall = titleLg,
    titleLarge = titleLg,
    titleMedium = titleMd,
    titleSmall = labelMd,
    bodyLarge = bodyLg,
    bodyMedium = bodyMd,
    bodySmall = labelMd,
    labelLarge = labelMd,
    labelMedium = labelMd,
    labelSmall = labelSm,
)

internal val LocalAppTypography = staticCompositionLocalOf { compactTypography() }
