package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Several fields that are one answer, inside one frame.
 *
 * An address is four fields and one fact; a card is a number, an expiry and a code. Given four
 * separate framed fields a person reads four questions, and the form looks twice as long as it is.
 * One frame with hairlines between says they belong together, and the group is what carries the
 * label and the error rather than each field carrying its own.
 *
 * Put [AppTextField]s inside with `frame = false` — the group draws the frame, and a field that
 * draws its own inside it makes a box in a box. The divider between rows is this component's, not
 * the caller's.
 */
@Composable
fun AppFieldGroup(
    modifier: Modifier = Modifier,
    label: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    val isError = errorText != null
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement
            .spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) {
            AppText(text = label, role = TextRole.Label, color = colors.textSecondary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.md)
                .background(colors.surfaceSunken)
                .border(
                    width = if (isError) 3.dp else 1.dp,
                    color = if (isError) colors.destructive.bg else colors.borderStrong,
                    shape = AppTheme.shapes.md,
                ),
            content = content,
        )
        val supporting = errorText ?: helperText
        if (supporting != null) {
            AppText(
                text = supporting,
                role = TextRole.Label,
                color = if (isError) colors.destructive.bg else colors.textSecondary,
            )
        }
    }
}

/**
 * One row of an [AppFieldGroup].
 *
 * It exists so the group owns the hairline: a caller that put its own [AppDivider] between rows
 * would have to remember not to put one after the last.
 */
@Composable
fun AppFieldGroupRow(
    modifier: Modifier = Modifier,
    last: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppTheme.spacing.inset.md,
                    vertical = AppTheme.spacing.inset.sm,
                ),
            content = content,
        )
        if (!last) AppDivider()
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppFieldGroup(label = "Delivery address", helperText = "Where the order goes") {
        AppFieldGroupRow {
            AppTextField(value = "Nádražní 12", onValueChange = {}, frame = false)
        }
        AppFieldGroupRow {
            AppTextField(value = "Praha 5", onValueChange = {}, frame = false)
        }
        AppFieldGroupRow(last = true) {
            AppTextField(value = "150 00", onValueChange = {}, frame = false, numeric = true)
        }
    }
    AppFieldGroup(label = "Card", errorText = "This card has expired") {
        AppFieldGroupRow { AppText(text = "4242 4242 4242 4242", role = TextRole.Numeric) }
        AppFieldGroupRow(last = true) { AppText(text = "01 / 24", role = TextRole.Numeric) }
    }
}
