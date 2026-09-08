package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A label for an icon that has none.
 *
 * It never carries anything you cannot continue without — on a touch screen there is no hover, so
 * a tooltip is help for the people using a mouse and nothing more.
 */
@Composable
fun AppTooltip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(AppTheme.shapes.sm)
            .background(AppTheme.colors.surfaceInverse)
            .padding(
                horizontal = AppTheme.spacing.inset.md,
                vertical = AppTheme.spacing.inset.sm,
            ),
    ) {
        AppText(text = text, role = TextRole.Label, color = AppTheme.colors.textOnInverse)
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppTooltip(text = "Split the bill")
}
