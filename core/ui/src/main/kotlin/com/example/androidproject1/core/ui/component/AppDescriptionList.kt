package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** A label and its value. A null [value] renders the missing-value mark, never a blank. */
data class DescriptionRow(val label: String, val value: String?, val numeric: Boolean = false)

/**
 * Label on the left, value on the right, one rhythm everywhere.
 *
 * An order's detail, a device, a profile — they all read the same way, which is what makes a new
 * detail screen take minutes.
 *
 * When the two do not fit on one line, **the value wraps and the label keeps its shape**: the
 * label is measured at its own width up to [LABEL_MAX_FRACTION] of the row, and the value takes
 * whatever is left. Before F4X1 it was the other way round — the value took its full width and the
 * label got the remainder, which at a 1.5× font scale was one letter per line.
 */
@Composable
fun AppDescriptionList(
    rows: List<DescriptionRow>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val labelMaxWidth = maxWidth * LABEL_MAX_FRACTION
        Column(modifier = Modifier.fillMaxWidth()) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppTheme.spacing.inset.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.lg),
                ) {
                    AppText(
                        text = row.label,
                        role = TextRole.Secondary,
                        modifier = Modifier.widthIn(max = labelMaxWidth),
                    )
                    if (row.value == null) {
                        // Never an empty cell and never a zero: a dash says "we do not know".
                        AppText(
                            text = "—",
                            role = TextRole.Tertiary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        AppText(
                            text = row.value,
                            role = if (row.numeric) TextRole.Numeric else TextRole.Body,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (index != rows.lastIndex) AppDivider()
            }
        }
    }
}

/**
 * How much of the row a label may take before it wraps. Two fifths keeps "Leak detection" on one
 * line on a phone and still leaves a URL room to break at a slash rather than a letter.
 */
private const val LABEL_MAX_FRACTION = 0.4f

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppDescriptionList(
        rows = listOf(
            DescriptionRow("Opened", "19:24"),
            DescriptionRow("Server", "Jana N."),
            DescriptionRow("Total", "1 248,00", numeric = true),
            DescriptionRow("Tip", null),
        ),
    )
}
