package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** One destination in [AppBottomNav] or [AppNavRail]. */
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val badge: Int = 0,
)

/**
 * The bottom bar, for the compact class.
 *
 * At most four destinations, and the selected one always shows its label — an icon alone is a
 * guess, and the bar is the thing people navigate by without looking.
 */
@Composable
fun AppBottomNav(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceRaised)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 64.dp)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                    .padding(vertical = AppTheme.spacing.inset.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .clip(AppTheme.shapes.pill)
                            .background(
                                if (selected) colors.destructive.container else colors.surfaceRaised,
                            )
                            .padding(
                                horizontal = AppTheme.spacing.inline.md,
                                vertical = AppTheme.spacing.inline.xs,
                            ),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) colors.destructive.onContainer else colors.textSecondary,
                            modifier = Modifier.size(AppTheme.icons.md),
                        )
                    }
                    AppBadge(count = item.badge)
                }
                AppText(
                    text = item.label,
                    role = TextRole.Label,
                    color = if (selected) colors.textPrimary else colors.textSecondary,
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppBottomNav(
        items = listOf(
            NavItem("Home", Icons.Filled.Home),
            NavItem("Orders", Icons.AutoMirrored.Filled.List, badge = 3),
            NavItem("Settings", Icons.Filled.Settings),
        ),
        selectedIndex = 0,
        onSelect = {},
    )
}
