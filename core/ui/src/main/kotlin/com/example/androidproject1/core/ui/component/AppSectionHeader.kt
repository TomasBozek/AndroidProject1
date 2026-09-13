package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A heading inside a screen: label on the left, at most one action on the right.
 *
 * At most one — a section with two actions is a section that wants to be a screen.
 *
 * @param actionTestTag what a flow and a test find the action by — `<stem>_<name>Button`. It needs
 * a tag of its own for the reason `AppTopBar`'s arrow does: the caller's [modifier] goes to the
 * row, and the label is translated.
 */
@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionTestTag: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.stack.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppText(text = title, role = TextRole.LabelSmall)
        if (actionLabel != null && onAction != null) {
            AppButton(
                label = actionLabel,
                onClick = onAction,
                kind = ButtonKind.Ghost,
                size = ControlSize.Small,
                modifier = if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSectionHeader(title = "OPEN ORDERS")
    AppSectionHeader(title = "DEVICES", actionLabel = "Add", onAction = {})
}
