package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The same destinations as [AppBottomNav], down the side, for the regular and expanded classes.
 *
 * Moving between size classes changes where navigation sits, never what is in it — someone who
 * learns the order on a phone finds the same order on a till.
 */
@Composable
fun AppNavRail(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(colors.surfaceRaised)
            .padding(vertical = AppTheme.spacing.inset.lg)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelect(index) },
                    ),
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
    AppNavRail(
        items = listOf(
            NavItem("Home", Icons.Filled.Home),
            NavItem("Orders", Icons.AutoMirrored.Filled.List, badge = 3),
        ),
        selectedIndex = 1,
        onSelect = {},
    )
}
