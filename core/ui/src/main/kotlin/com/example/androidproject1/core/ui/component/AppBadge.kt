package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** A count over an icon or in a tab. Above 99 it stops being a number and becomes "99+". */
@Composable
fun AppBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(AppTheme.shapes.pill)
            .background(AppTheme.colors.destructive.bg)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = if (count > 99) "99+" else count.toString(),
            role = TextRole.Numeric,
            color = AppTheme.colors.destructive.label,
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppBadge(count = 3)
    AppBadge(count = 42)
    AppBadge(count = 128)
}
