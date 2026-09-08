package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Initials, not a photograph — in daily use nobody uploads one.
 *
 * The colour is derived from [id] rather than stored, so the same person is the same colour on
 * every device without a field to keep in sync.
 */
@Composable
fun AppAvatar(
    name: String,
    modifier: Modifier = Modifier,
    id: String = name,
    size: Dp = 40.dp,
) {
    val colors = AppTheme.colors
    val families = listOf(colors.confirm, colors.info, colors.warning, colors.destructive)
    val family = families[(id.hashCode().and(Int.MAX_VALUE)) % families.size]
    val initials = name.trim().split(" ", limit = 2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .take(2)
    Box(
        modifier = modifier
            .size(size)
            .clip(AppTheme.shapes.pill)
            .background(family.container),
        contentAlignment = Alignment.Center,
    ) {
        AppText(text = initials, role = TextRole.Label, color = family.onContainer)
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppAvatar(name = "Jana Nováková")
    AppAvatar(name = "Petr Svoboda")
    AppAvatar(name = "Root", size = 56.dp)
}
