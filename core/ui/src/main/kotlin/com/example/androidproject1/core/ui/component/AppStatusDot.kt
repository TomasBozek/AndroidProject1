package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A state, as a dot and a word.
 *
 * The dot never stands alone — [label] is required, not optional, because a colour on its own is
 * invisible to anyone who cannot separate the hues.
 */
@Composable
fun AppStatusDot(
    label: String,
    modifier: Modifier = Modifier,
    tone: TagTone = TagTone.Neutral,
) {
    val family = tone.feedback(AppTheme.colors)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(AppTheme.shapes.pill)
                .background(family.accent),
        )
        AppText(text = label, role = TextRole.Label)
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppStatusDot(label = "Active", tone = TagTone.Positive)
    AppStatusDot(label = "Pending", tone = TagTone.Warning)
    AppStatusDot(label = "Failed", tone = TagTone.Negative)
    AppStatusDot(label = "Offline")
}
