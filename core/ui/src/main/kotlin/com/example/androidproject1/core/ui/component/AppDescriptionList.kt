package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** A label and its value. A null [value] renders the missing-value mark, never a blank. */
data class DescriptionRow(val label: String, val value: String?, val numeric: Boolean = false)

/**
 * Label on the left, value on the right, one rhythm everywhere.
 *
 * A receipt detail, a device, an employee — they all read the same way, which is what makes a new
 * detail screen take minutes.
 */
@Composable
fun AppDescriptionList(
    rows: List<DescriptionRow>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppTheme.spacing.inset.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.lg),
            ) {
                AppText(text = row.label, role = TextRole.Secondary, modifier = Modifier.weight(1f))
                if (row.value == null) {
                    // Never an empty cell and never a zero: a dash says "we do not know".
                    AppText(text = "—", role = TextRole.Tertiary)
                } else {
                    AppText(
                        text = row.value,
                        role = if (row.numeric) TextRole.Numeric else TextRole.Body,
                    )
                }
            }
            if (index != rows.lastIndex) AppDivider()
        }
    }
}

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
