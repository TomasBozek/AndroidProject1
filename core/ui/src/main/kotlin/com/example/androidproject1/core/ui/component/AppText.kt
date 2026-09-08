package com.example.androidproject1.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
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
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = resolved,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppText(text = "1 248,00", role = TextRole.DisplayLarge)
    AppText(text = "Order 12 — Terrace", role = TextRole.Display)
    AppText(text = "Drinks · Hot drinks", role = TextRole.Title)
    AppText(text = "The default size for list rows and dialog copy.", role = TextRole.Body)
    AppText(text = "Payment method", role = TextRole.Label)
    AppText(text = "OPEN ORDERS", role = TextRole.LabelSmall)
    AppText(text = "118,00", role = TextRole.Numeric)
    AppText(text = "Opened 19:24 · Jana N.", role = TextRole.Secondary)
    AppText(text = "—", role = TextRole.Tertiary)
}
