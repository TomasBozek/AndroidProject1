package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Two to four short options that switch immediately.
 *
 * Longer labels or a badge mean [AppTabs] instead; this control has no room to grow and a fifth
 * option makes every one of them unreadable.
 */
@Composable
fun AppSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.md)
            .background(colors.surfaceSunken)
            .border(1.dp, colors.borderStrong, AppTheme.shapes.md)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                    .background(if (selected) colors.confirm.bg else colors.surfaceSunken)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) },
                    )
                    .padding(horizontal = AppTheme.spacing.inline.md),
                contentAlignment = Alignment.Center,
            ) {
                AppText(
                    text = option,
                    role = TextRole.Label,
                    color = if (selected) colors.confirm.label else colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSegmented(options = listOf("Today", "Week", "Month"), selectedIndex = 0, onSelect = {})
    AppSegmented(options = listOf("Percent", "Amount"), selectedIndex = 1, onSelect = {})
}
