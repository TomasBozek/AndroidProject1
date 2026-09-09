package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A hairline where a gap is not enough.
 *
 * Decorative, so it is allowed to be quiet — it draws `colors.border`, not `borderStrong`. Use it
 * sparingly: a divider between every row of a list turns the list into a table.
 *
 * [strong] is for the one line that separates two *sections* rather than two rows. There is at most
 * one of those on a screen; a second one makes the first mean nothing.
 */
@Composable
fun AppDivider(modifier: Modifier = Modifier, strong: Boolean = false) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(if (strong) AppTheme.colors.borderStrong else AppTheme.colors.border),
    )
}

/**
 * A divider between two things side by side — two panes, a row of icon buttons.
 *
 * It fills the height it is given, so it belongs in a `Row` with a height and nowhere else: in one
 * that wraps its content it collapses to nothing, which reads as a divider someone forgot rather
 * than one that is broken.
 */
@Composable
fun AppVerticalDivider(modifier: Modifier = Modifier, strong: Boolean = false) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(if (strong) AppTheme.colors.borderStrong else AppTheme.colors.border),
    )
}

/**
 * A divider that says what is below it.
 *
 * Prefer [AppSectionHeader] when the section has a heading of its own; this is for the quieter
 * break inside one — "earlier today" in a list of orders — where a heading would over-announce it.
 */
@Composable
fun AppLabelledDivider(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        AppDivider(modifier = Modifier.weight(1f))
        AppText(
            text = label,
            role = TextRole.LabelSmall,
            color = AppTheme.colors.textSecondary,
        )
        AppDivider(modifier = Modifier.weight(1f))
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppText(text = "Above")
    AppDivider()
    AppText(text = "Below")
    AppDivider(strong = true)
    AppLabelledDivider(label = "EARLIER TODAY")
    Row(modifier = Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
        AppText(text = "Left")
        AppVerticalDivider(modifier = Modifier.padding(horizontal = AppTheme.spacing.inline.md))
        AppText(text = "Right")
    }
}
