package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The bar that appears when something is selected.
 *
 * It is inverted on purpose: a different mode needs to look like one, or the actions on it get
 * read as the screen's own.
 */
@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.lg)
            .background(AppTheme.colors.surfaceInverse)
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = AppTheme.spacing.inset.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        AppText(
            text = title,
            role = TextRole.Body,
            color = AppTheme.colors.textOnInverse,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppToolbar(title = "3 selected") {
        AppText(text = "Move", role = TextRole.Label, color = AppTheme.colors.textOnInverse)
        AppText(text = "Delete", role = TextRole.Label, color = AppTheme.colors.destructive.bg)
    }
}
