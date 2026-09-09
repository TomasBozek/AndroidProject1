package com.example.androidproject1.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.androidproject1.core.ui.R

/**
 * The one family the whole system uses: Source Sans 3, bundled.
 *
 * Bundled rather than fetched, because a preview and a screenshot golden have to render the same
 * face with no network, and a device without Play Services has no downloadable-fonts provider to
 * ask. The licence is `licenses/SourceSans3-OFL.txt`.
 *
 * **One variable font, not four statics.** The four weights the scale asks for — 400, 600, 700,
 * 800 — come to 1.6 MB as separate Adobe TTFs, because each carries Latin, Greek and Cyrillic.
 * The variable file is 646 kB and covers every weight from 200 to 900. `minSdk` is 29 and
 * variable fonts need 26, so there is no floor to raise.
 *
 * The scale is defined in roles rather than sizes, so swapping this for another brand face is
 * still one edit and no screen changes.
 */
val AppFontFamily: FontFamily = FontFamily(
    SourceSans3(FontWeight.Normal),
    SourceSans3(FontWeight.SemiBold),
    SourceSans3(FontWeight.Bold),
    SourceSans3(FontWeight.ExtraBold),
)

/**
 * One weight off the variable axis.
 *
 * The `wght` setting is passed explicitly rather than left to the default: without it a device
 * that cannot apply variations falls back to synthesising the weight, and 600 and 700 render
 * identically.
 */
private fun SourceSans3(weight: FontWeight) = Font(
    resId = R.font.source_sans_3,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

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
