package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * One line, full width, in the warning role, drawn above the screen rather than over it: a fact
 * about the whole app that stays true until it stops — the device is offline.
 *
 * Not a toast (D63): a toast reports something that happened and goes away; a banner reports
 * something that is so, for as long as it is so, and has no action of its own. What to do about
 * it is the screen's business. `MainActivity` draws one above `AppNavHost` while
 * `MainViewModel.online` is false (D68).
 */
@Composable
fun AppBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    AppText(
        text = text,
        role = TextRole.Label,
        color = colors.warning.onContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.warning.container)
            .padding(
                horizontal = AppTheme.spacing.inset.lg,
                vertical = AppTheme.spacing.inset.sm,
            ),
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppBanner(text = "You're offline")
}
