package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
 *
 * [status] adds the dot the document calls the "with status" variant — whether that person is
 * online. It carries [statusDescription] for the screen reader, because a dot in a corner is the
 * one part of an avatar that says something a name does not.
 */
@Composable
fun AppAvatar(
    name: String,
    modifier: Modifier = Modifier,
    id: String = name,
    size: Dp = 40.dp,
    status: TagTone? = null,
    statusDescription: String? = null,
) {
    val colors = AppTheme.colors
    val families = listOf(colors.confirm, colors.info, colors.warning, colors.destructive)
    val family = families[(id.hashCode().and(Int.MAX_VALUE)) % families.size]
    val initials = name.trim().split(" ", limit = 2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .take(2)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(AppTheme.shapes.pill)
                .background(family.container),
            contentAlignment = Alignment.Center,
        ) {
            AppText(text = initials, role = TextRole.Label, color = family.onContainer)
        }
        if (status != null) {
            val tone = when (status) {
                TagTone.Neutral -> colors.neutral
                TagTone.Positive -> colors.statusPositive
                TagTone.Warning -> colors.statusWarning
                TagTone.Negative -> colors.statusNegative
                TagTone.Info -> colors.info
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // The ring is the surface behind it, not white: on a dark theme a white ring
                    // is a second dot.
                    .size(size / 4 + 4.dp)
                    .clip(AppTheme.shapes.pill)
                    .background(colors.surfaceBase),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(size / 4)
                        .clip(AppTheme.shapes.pill)
                        .background(tone.bg)
                        .semantics {
                            if (statusDescription != null) contentDescription = statusDescription
                        },
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppAvatar(name = "Jana Nováková")
    AppAvatar(name = "Petr Svoboda")
    AppAvatar(name = "Root", size = 56.dp)
    AppAvatar(
        name = "Jana Nováková",
        status = TagTone.Positive,
        statusDescription = "Online",
    )
    AppAvatar(
        name = "Petr Svoboda",
        size = 56.dp,
        status = TagTone.Negative,
        statusDescription = "Away",
    )
}
