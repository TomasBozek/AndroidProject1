package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The wrapper around any field: label above, help or error below.
 *
 * Use it for controls that are not [AppTextField] — a select, a segmented control, a switch row —
 * so every field in a form has the same anatomy. **A required field says the word**; an asterisk
 * is a convention only some people know.
 */
@Composable
fun AppFormField(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    helperText: String? = null,
    errorText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        AppText(
            text = if (required) "$label · required" else label,
            role = TextRole.Label,
            color = AppTheme.colors.textSecondary,
        )
        content()
        val supporting = errorText ?: helperText
        if (supporting != null) {
            AppText(
                text = supporting,
                role = TextRole.Label,
                color = if (errorText != null) {
                    AppTheme.colors.destructive.bg
                } else {
                    AppTheme.colors.textSecondary
                },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppFormField(label = "Payment method", required = true) {
        AppSegmented(options = listOf("Cash", "Card"), selectedIndex = 0, onSelect = {})
    }
    AppFormField(label = "Discount", errorText = "Above the limit for this role") {
        AppSegmented(options = listOf("10 %", "20 %", "50 %"), selectedIndex = 2, onSelect = {})
    }
}
