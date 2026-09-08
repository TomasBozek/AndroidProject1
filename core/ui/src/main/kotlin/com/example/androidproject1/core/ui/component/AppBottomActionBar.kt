package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * The anchored bar carrying a screen's one main action.
 *
 * The total is on the left and the action on the right, and the action is the wider of the two —
 * on a phone this is what the thumb reaches without looking.
 */
@Composable
fun AppBottomActionBar(
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    value: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceRaised)
            .padding(AppTheme.spacing.inset.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.lg),
    ) {
        if (value != null) {
            Column(modifier = Modifier.weight(1f)) {
                if (label != null) AppText(text = label, role = TextRole.LabelSmall)
                AppText(text = value, role = TextRole.DisplayLarge)
            }
        }
        AppButton(
            label = actionLabel,
            onClick = onAction,
            size = ButtonSize.Large,
            enabled = enabled,
            modifier = Modifier.weight(if (value != null) 1.4f else 1f),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppBottomActionBar(
        label = "TOTAL",
        value = "1 248,00",
        actionLabel = "Pay",
        onAction = {},
    )
    AppBottomActionBar(actionLabel = "Continue", onAction = {})
}
