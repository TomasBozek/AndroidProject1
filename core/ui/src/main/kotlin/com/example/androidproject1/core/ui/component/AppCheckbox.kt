package com.example.androidproject1.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/** A checkbox is on, off, or — only inside a group toggle — partly on. */
enum class CheckState { Off, On, Indeterminate }

/**
 * Selecting several things at once.
 *
 * The whole row is the target, not the 20 dp box, so the touch area clears the minimum without the
 * box having to grow. [CheckState.Indeterminate] belongs to a group's own toggle and nowhere else:
 * on a single item there is nothing for "partly" to mean.
 */
@Composable
fun AppCheckbox(
    checked: CheckState,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val on = checked != CheckState.Off
    val fill by animateFloatAsState(
        targetValue = if (on) 1f else 0f,
        animationSpec = tween(AppTheme.motion.toggleMillis, easing = AppTheme.motion.toggleEasing),
        label = "checkboxFill",
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
            .toggleable(
                value = on,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(AppTheme.shapes.sm)
                .background(colors.confirm.bg.copy(alpha = fill))
                .border(
                    width = if (on) 0.dp else 2.dp,
                    color = if (enabled) colors.borderStrong else colors.border,
                    shape = AppTheme.shapes.sm,
                )
                .alpha(if (enabled) 1f else 0.5f),
            contentAlignment = Alignment.Center,
        ) {
            when (checked) {
                CheckState.On -> Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.confirm.label,
                    modifier = Modifier.size(16.dp),
                )
                // A dash, not a second glyph: "some of the group", drawn rather than imported.
                CheckState.Indeterminate -> Box(
                    modifier = Modifier
                        .size(width = 11.dp, height = 2.dp)
                        .background(colors.confirm.label),
                )
                CheckState.Off -> Unit
            }
        }
        Text(
            text = label,
            style = AppTheme.typography.bodyMd,
            color = if (enabled) colors.textPrimary else colors.textDisabled,
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppCheckbox(checked = CheckState.On, onCheckedChange = {}, label = "Print receipt")
    AppCheckbox(checked = CheckState.Off, onCheckedChange = {}, label = "Email receipt")
    AppCheckbox(checked = CheckState.Indeterminate, onCheckedChange = {}, label = "All items")
    AppCheckbox(
        checked = CheckState.Off,
        onCheckedChange = {},
        label = "Unavailable",
        enabled = false,
    )
}
