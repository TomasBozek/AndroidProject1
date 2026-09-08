package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A raised surface for grouped content.
 *
 * Three surface levels exist and no more — sunken, base, raised — so a card does not stack on a
 * card. The fourth level is a dialog, and that is a different component.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.xl)
            .background(AppTheme.colors.surfaceRaised)
            .padding(AppTheme.spacing.inset.lg),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement
            .spacedBy(AppTheme.spacing.stack.sm),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppCard {
        AppText(text = "Notifications", role = TextRole.Title)
        AppText(text = "Turn these on to hear about orders while the app is closed.", role = TextRole.Secondary)
    }
}
