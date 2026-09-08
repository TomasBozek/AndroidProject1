package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A dialog: the top layer, and the only one allowed over a sheet.
 *
 * [title] is a sentence about what is going to happen, never a single noun, and the confirming
 * button carries a verb — "Void order", not "OK". That is the difference between a dialog someone
 * reads and one they dismiss.
 */
@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
    actions: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .widthIn(max = 440.dp)
                .clip(AppTheme.shapes.xl)
                .background(AppTheme.colors.surfaceRaised)
                .padding(AppTheme.spacing.inset.xl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppText(text = title, role = TextRole.TitleLarge)
            if (message != null) AppText(text = message, role = TextRole.Body)
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    AppTheme.spacing.destructiveGap,
                    Alignment.End,
                ),
            ) {
                actions()
            }
        }
    }
}

/**
 * The dialog for something that cannot be undone.
 *
 * The gap between the two buttons is [AppTheme.spacing]'s `destructiveGap` — in a hurry, people
 * press blind, and the confirming button must not sit under the thumb that meant to cancel.
 */
@Composable
fun AppConfirmDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissLabel: String = "Cancel",
    destructive: Boolean = true,
) {
    AppDialog(
        title = title,
        message = message,
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        AppButton(label = dismissLabel, onClick = onDismiss, kind = ButtonKind.Ghost)
        AppButton(
            label = confirmLabel,
            onClick = onConfirm,
            kind = if (destructive) ButtonKind.Destructive else ButtonKind.Confirm,
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // A dialog draws in its own window, so the preview shows the buttons it would carry.
    AppText(text = "Void this order?", role = TextRole.TitleLarge)
    AppText(text = "The items go back to stock and the order is closed.", role = TextRole.Body)
    AppButton(label = "Cancel", onClick = {}, kind = ButtonKind.Ghost)
    AppButton(label = "Void order", onClick = {}, kind = ButtonKind.Destructive)
}
