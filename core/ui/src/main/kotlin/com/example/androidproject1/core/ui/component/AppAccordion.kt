package com.example.androidproject1.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Collapsible content — for settings, not for anything operational.
 *
 * It hides information behind a tap, which is the wrong trade when someone is working against the
 * clock. In an operational screen, show it or move it to its own screen.
 */
@Composable
fun AppAccordion(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.lg)
            .background(AppTheme.colors.surfaceRaised),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(AppTheme.spacing.inset.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
        ) {
            AppText(text = title, role = TextRole.Body, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(AppTheme.icons.md),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(
                    start = AppTheme.spacing.inset.lg,
                    end = AppTheme.spacing.inset.lg,
                    bottom = AppTheme.spacing.inset.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.sm),
                content = content,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppAccordion(title = "Advanced", expanded = true, onToggle = {}) {
        AppText(text = "Settings that most people never need to open.", role = TextRole.Secondary)
    }
    AppAccordion(title = "Diagnostics", expanded = false, onToggle = {}) {}
}
