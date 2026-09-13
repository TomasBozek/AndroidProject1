package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** What a toast is reporting. An error is the only one that does not dismiss itself. */
enum class ToastTone { Neutral, Success, Error }

/**
 * Confirmation of something that has already happened.
 *
 * It sits above every other layer, because it reports things that happened off-screen — an order
 * sent, a connection lost, a print that failed. Everything but an error disappears within five
 * seconds; an error waits to be read.
 */
@Composable
fun AppToast(
    message: String,
    modifier: Modifier = Modifier,
    tone: ToastTone = ToastTone.Neutral,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val background = when (tone) {
        ToastTone.Neutral -> colors.surfaceInverse
        ToastTone.Success -> colors.confirm.bg
        ToastTone.Error -> colors.destructive.bg
    }
    val foreground = when (tone) {
        ToastTone.Neutral -> colors.textOnInverse
        ToastTone.Success -> colors.confirm.label
        ToastTone.Error -> colors.destructive.label
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.lg)
            .background(background)
            .padding(
                horizontal = AppTheme.spacing.inset.lg,
                vertical = AppTheme.spacing.inset.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        AppText(
            text = message,
            role = TextRole.Body,
            color = foreground,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            AppText(
                text = actionLabel,
                role = TextRole.Label,
                color = foreground,
                modifier = Modifier
                    .clip(AppTheme.shapes.sm)
                    .clickable(role = Role.Button, onClick = onAction)
                    // The action is a real control, so it gets a real target rather than the
                    // height of its own text.
                    .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                    .padding(AppTheme.spacing.inset.sm),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppToast(message = "Order placed", tone = ToastTone.Success)
    AppToast(message = "Item moved to your wishlist", actionLabel = "Undo", onAction = {})
    AppToast(message = "Printer not responding", tone = ToastTone.Error)
}
