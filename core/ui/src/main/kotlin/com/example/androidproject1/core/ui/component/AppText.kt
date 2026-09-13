package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * What this text *is*, which decides both its size and its colour.
 *
 * Four roles cover an interface; the numeric two exist because money and quantities need tabular
 * figures and a screen that types `bodyMd` for a price will get columns that do not line up.
 */
enum class TextRole {
    /** The amount on a display, a total. */
    DisplayLarge,

    /** A screen's own name. */
    Display,

    /** A dialog title. */
    TitleLarge,

    /** A section, a category, a tile. */
    Title,

    /** Running text and descriptions. */
    BodyLarge,

    /** List rows — the default. */
    Body,

    /** A field label, a small button. */
    Label,

    /** A group heading, in caps. */
    LabelSmall,

    /** A price or a quantity. Tabular figures. */
    Numeric,

    /** Metadata beside the thing it describes. */
    Secondary,

    /** A missing value. Never an empty cell, never a zero. */
    Tertiary,
}

/**
 * Text, asked for by role.
 *
 * A screen never names a size, a weight or a colour — it says what the text is for, and the theme
 * decides. That is what makes a density change or a re-brand a change to the theme alone.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    role: TextRole = TextRole.Body,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val type = AppTheme.typography
    val colors = AppTheme.colors
    val style: TextStyle = when (role) {
        TextRole.DisplayLarge -> type.displayXl
        TextRole.Display -> type.displayLg
        TextRole.TitleLarge -> type.titleLg
        TextRole.Title -> type.titleMd
        TextRole.BodyLarge -> type.bodyLg
        TextRole.Body -> type.bodyMd
        TextRole.Label -> type.labelMd
        TextRole.LabelSmall -> type.labelSm
        TextRole.Numeric -> type.numericMd
        TextRole.Secondary -> type.bodyMd
        TextRole.Tertiary -> type.bodyMd
    }
    val resolved = color ?: when (role) {
        TextRole.Secondary, TextRole.LabelSmall -> colors.textSecondary
        TextRole.Tertiary -> colors.textTertiary
        else -> colors.textPrimary
    }
    if (role == TextRole.Numeric) {
        // A price shrinks rather than wraps. "1 234,00 Kč" is four characters longer than
        // "1,234.00", so the same row that fits in English wraps in Czech — and a wrapped figure
        // in a table of figures is worse than a slightly smaller one.
        BasicText(
            text = text,
            modifier = modifier,
            style = style.copy(color = resolved),
            maxLines = 1,
            overflow = overflow,
            autoSize = TextAutoSize.StepBased(minFontSize = NUMERIC_MIN_SIZE, maxFontSize = style.fontSize),
        )
        return
    }

    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = resolved,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/**
 * How far a figure may shrink before the answer is a different layout.
 *
 * Below this it stops being comfortably readable at arm's length, which is what a total on a screen
 * someone is standing at has to be.
 */
private val NUMERIC_MIN_SIZE = 12.sp

/** Narrow enough that the preview shows the shrink rather than a figure with room to spare. */
private val NUMERIC_PREVIEW_WIDTH = 120.dp

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppText(text = "1 248,00", role = TextRole.DisplayLarge)
    AppText(text = "Order 12 — On its way", role = TextRole.Display)
    AppText(text = "Drinks · Hot drinks", role = TextRole.Title)
    AppText(text = "The default size for list rows and dialog copy.", role = TextRole.Body)
    AppText(text = "Appearance", role = TextRole.Label)
    AppText(text = "OPEN ORDERS", role = TextRole.LabelSmall)
    AppText(text = "118,00", role = TextRole.Numeric)
    // The reason Numeric shrinks: a Czech total in a narrow column. In English the same row reads
    // "1,234.00" and fits; this is four characters longer and used to wrap.
    Box(modifier = Modifier.width(NUMERIC_PREVIEW_WIDTH)) {
        AppText(text = "1 234 567,00 Kč", role = TextRole.Numeric)
    }
    AppText(text = "Opened 19:24 · Jana N.", role = TextRole.Secondary)
    AppText(text = "—", role = TextRole.Tertiary)
}
