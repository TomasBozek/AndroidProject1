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
import com.example.androidproject1.core.ui.theme.AppColors
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.core.ui.theme.FeedbackColors

/** What a tag is saying. Colour never carries this on its own — the text always does too. */
enum class TagTone { Neutral, Positive, Warning, Negative, Info }

/**
 * The feedback role a tone draws in (D78) — the one mapping a tag, a status dot and an avatar's
 * status share, so the three cannot drift apart. `Neutral` is not feedback: it borrows the
 * neutral action's soft pair, and takes the secondary text colour as its accent, so a neutral dot
 * is a grey mark rather than the neutral button's white face.
 */
fun TagTone.feedback(colors: AppColors): FeedbackColors = when (this) {
    TagTone.Neutral -> FeedbackColors(colors.neutral.container, colors.neutral.onContainer, colors.textSecondary)
    TagTone.Positive -> colors.feedback.success
    TagTone.Warning -> colors.feedback.warning
    TagTone.Negative -> colors.feedback.error
    TagTone.Info -> colors.feedback.info
}

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
    val family = tone.feedback(AppTheme.colors)
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
