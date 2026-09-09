package com.example.androidproject1.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
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

/**
 * An immediate setting change, with no confirmation step.
 *
 * That is also the rule for when *not* to use one: a switch never guards a destructive choice,
 * because there is nowhere to put the confirmation. Use [AppButton] with a confirm dialog instead.
 *
 * [size] moves the track, not the row: the whole row stays the target at every size, so a
 * [ControlSize.Small] switch in a dense list is still a comfortable thing to hit.
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    enabled: Boolean = true,
    size: ControlSize = ControlSize.Medium,
) {
    val colors = AppTheme.colors
    val small = size == ControlSize.Small
    val trackWidth = if (small) 36.dp else 44.dp
    val trackHeight = if (small) 22.dp else 26.dp
    val knob = if (small) 18.dp else 22.dp
    val travel = trackWidth - knob - 2.dp
    val spec = tween<androidx.compose.ui.unit.Dp>(
        AppTheme.motion.toggleMillis,
        easing = AppTheme.motion.toggleEasing,
    )
    val knobOffset by animateDpAsState(if (checked) travel else 2.dp, spec, label = "switchKnob")
    val track by animateColorAsState(
        targetValue = if (checked) colors.confirm.bg else colors.borderStrong,
        animationSpec = tween(AppTheme.motion.toggleMillis, easing = AppTheme.motion.toggleEasing),
        label = "switchTrack",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.md),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
        ) {
            AppText(text = label, role = TextRole.Body)
            if (supporting != null) AppText(text = supporting, role = TextRole.Secondary)
        }
        Box(
            modifier = Modifier
                .size(width = trackWidth, height = trackHeight)
                .clip(AppTheme.shapes.pill)
                .background(track)
                .alpha(if (enabled) 1f else 0.5f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(knob)
                    .clip(AppTheme.shapes.pill)
                    .background(colors.surfaceRaised),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSwitch(checked = true, onCheckedChange = {}, label = "Print receipt automatically")
    AppSwitch(
        checked = false,
        onCheckedChange = {},
        label = "Sounds",
        supporting = "Feedback on every key press",
    )
    AppSwitch(checked = false, onCheckedChange = {}, label = "Unavailable", enabled = false)
    AppSwitch(
        checked = true,
        onCheckedChange = {},
        label = "Small, for a dense list",
        size = ControlSize.Small,
    )
}
