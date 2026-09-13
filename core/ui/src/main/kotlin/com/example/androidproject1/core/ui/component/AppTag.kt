package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** What a tag is saying. Colour never carries this on its own — the text always does too. */
enum class TagTone { Neutral, Positive, Warning, Negative, Info }

/**
 * A state or a property that cannot be pressed.
 *
 * The tone tints it, but the label is what says which state it is: a colour-only status is
 * invisible to a good share of the people reading it, and this is the one place in the system
 * where colour carries meaning at all.
 */
@Composable
fun AppTag(
    label: String,
    modifier: Modifier = Modifier,
    tone: TagTone = TagTone.Neutral,
) {
    val colors = AppTheme.colors
    val family = when (tone) {
        TagTone.Neutral -> colors.neutral
        TagTone.Positive -> colors.statusPositive
        TagTone.Warning -> colors.statusWarning
        TagTone.Negative -> colors.statusNegative
        TagTone.Info -> colors.info
    }
    Box(
        modifier = modifier
            .clip(AppTheme.shapes.pill)
            .background(family.container)
            .padding(horizontal = AppTheme.spacing.inline.md, vertical = 3.dp),
    ) {
        AppText(text = label, role = TextRole.Label, color = family.onContainer)
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppTag(label = "Active", tone = TagTone.Positive)
    AppTag(label = "Pending", tone = TagTone.Warning)
    AppTag(label = "Failed", tone = TagTone.Negative)
    AppTag(label = "New", tone = TagTone.Info)
    AppTag(label = "Draft")
}
