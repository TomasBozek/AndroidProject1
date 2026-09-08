package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** One row of an [AppMenu]. A destructive item is drawn apart from the rest and always last. */
data class MenuItem(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Actions on the thing you opened it from.
 *
 * Destructive items sort to the bottom and are separated by a divider, so the one that cannot be
 * undone is never adjacent to the one you meant.
 */
@Composable
fun AppMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<MenuItem>,
    modifier: Modifier = Modifier,
    anchor: @Composable () -> Unit,
) {
    val ordered = items.sortedBy { it.destructive }
    Box(modifier = modifier) {
        anchor()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .clip(AppTheme.shapes.lg)
                .background(AppTheme.colors.surfaceRaised),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ordered.forEachIndexed { index, item ->
                    if (item.destructive && index > 0 && !ordered[index - 1].destructive) {
                        AppDivider()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                            .clickable(
                                enabled = item.enabled,
                                role = Role.Button,
                                onClick = item.onClick,
                            )
                            .padding(
                                horizontal = AppTheme.spacing.inset.lg,
                                vertical = AppTheme.spacing.inset.md,
                            ),
                    ) {
                        AppText(
                            text = item.label,
                            role = TextRole.Body,
                            color = when {
                                !item.enabled -> AppTheme.colors.textDisabled
                                item.destructive -> AppTheme.colors.destructive.bg
                                else -> AppTheme.colors.textPrimary
                            },
                        )
                    }
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppMenu(
        expanded = false,
        onDismiss = {},
        items = listOf(
            MenuItem("Rename") {},
            MenuItem("Duplicate") {},
            MenuItem("Delete", destructive = true) {},
        ),
        anchor = { AppButton(label = "Actions", onClick = {}, kind = ButtonKind.Outline) },
    )
}
