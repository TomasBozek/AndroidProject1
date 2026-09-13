package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** One tab. [badge] above zero draws an [AppBadge]; below or at zero draws nothing. */
data class TabItem(val label: String, val badge: Int = 0)

/**
 * Switching content inside one screen.
 *
 * Unlike [AppSegmented] this takes a longer label and a count, and the selected tab is marked by a
 * rule underneath rather than a fill — a filled tab competes with the buttons in the content.
 */
@Composable
fun AppTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalAlignment = Alignment.Bottom,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelect(index) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                        .padding(horizontal = AppTheme.spacing.inline.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
                ) {
                    AppText(
                        text = tab.label,
                        role = TextRole.BodyLarge,
                        color = if (selected) colors.textPrimary else colors.textSecondary,
                    )
                    AppBadge(count = tab.badge)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (selected) colors.destructive.bg else colors.border),
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppTabs(
        tabs = listOf(TabItem("All", badge = 4), TabItem("Unread"), TabItem("Archived")),
        selectedIndex = 0,
        onSelect = {},
    )
}
