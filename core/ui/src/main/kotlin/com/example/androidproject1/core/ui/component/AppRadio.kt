package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Exactly one choice from a group.
 *
 * Above five options this is the wrong control — use [AppSelect], which does not grow the screen
 * with every option added.
 */
@Composable
fun AppRadio(
    selected: Boolean,
    onSelect: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(AppTheme.shapes.pill)
                .background(if (selected) colors.confirm.bg else colors.surfaceBase)
                .border(
                    width = if (selected) 0.dp else 2.dp,
                    color = if (enabled) colors.borderStrong else colors.border,
                    shape = AppTheme.shapes.pill,
                )
                .alpha(if (enabled) 1f else 0.5f),
            contentAlignment = Alignment.Center,
        ) {
            // The dot is the surface showing through, not a second colour on top.
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(AppTheme.shapes.pill)
                        .background(colors.confirm.label),
                )
            }
        }
        AppText(
            text = label,
            role = TextRole.Body,
            color = if (enabled) colors.textPrimary else colors.textDisabled,
        )
    }
}

/** Wraps a set of [AppRadio] so a screen reader announces "one of N", not N separate controls. */
@Composable
fun AppRadioGroup(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
        content = content,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppRadioGroup {
        AppRadio(selected = true, onSelect = {}, label = "Light")
        AppRadio(selected = false, onSelect = {}, label = "Dark")
        AppRadio(selected = false, onSelect = {}, label = "System", enabled = false)
    }
}
