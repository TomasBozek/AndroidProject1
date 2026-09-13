package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.androidproject1.core.ui.R
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** Material's own cap, and the reason for it: a phone in landscape is about 360 dp tall. */
private val MAX_DIALOG_HEIGHT = 568.dp

/**
 * A dialog: the top layer, and the only one allowed over a sheet.
 *
 * [title] is a sentence about what is going to happen, never a single noun, and the confirming
 * button carries a verb — "Delete trip", not "OK". That is the difference between a dialog someone
 * reads and one they dismiss.
 *
 * Three things here are about the window rather than the content, and each one was a way for a
 * dialog to be unusable:
 *
 * - **The window sizes itself.** `usePlatformDefaultWidth` is off, because the platform default is
 *   a fraction of the screen and cannot hold content with a minimum width of its own. That is what
 *   clipped the date picker: Material's `DatePicker` asks for 360 dp, and on a 360 dp phone the
 *   inset either side of it had nowhere to come from.
 * - **The inset moved outside.** The card keeps its `inset.xl` padding, and the gap between the
 *   card and the edge of the screen is now a margin on the outside of it, so the padding is no
 *   longer competing with the content for width.
 * - **The content scrolls inside a height cap.** [actions] are outside that scroll, so the
 *   confirming button is on screen whatever the content does — in landscape, at a large font, or
 *   with a title that wraps to three lines.
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = modifier
                .padding(AppTheme.spacing.inset.md)
                .widthIn(max = 440.dp)
                .heightIn(max = MAX_DIALOG_HEIGHT)
                .clip(AppTheme.shapes.xl)
                .background(AppTheme.colors.surfaceRaised)
                .padding(AppTheme.spacing.inset.xl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
        ) {
            AppText(text = title, role = TextRole.TitleLarge)
            if (message != null) AppText(text = message, role = TextRole.Body)
            // `fill = false` so a short dialog stays short: the weight caps the content at what is
            // left over, it does not stretch it to fill.
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
                content = content,
            )
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
    dismissLabel: String = stringResource(R.string.app_dialog_cancel),
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
    AppText(text = "Delete this trip?", role = TextRole.TitleLarge)
    AppText(text = "Its itinerary and notes go with it. This cannot be undone.", role = TextRole.Body)
    AppButton(label = stringResource(R.string.app_dialog_cancel), onClick = {}, kind = ButtonKind.Ghost)
    AppButton(label = "Delete trip", onClick = {}, kind = ButtonKind.Destructive)
}
