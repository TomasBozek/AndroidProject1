package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.R
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * Quantity, one at a time.
 *
 * The value sits between the two keys at a fixed width so the row does not shift as the number
 * grows from 9 to 10, and it will not go below [min].
 */
@Composable
fun AppStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
    ) {
        AppIconButton(
            icon = Icons.Filled.Clear,
            contentDescription = stringResource(R.string.app_stepper_decrement),
            onClick = { onValueChange((value - 1).coerceAtLeast(min)) },
            enabled = value > min,
        )
        androidx.compose.material3.Text(
            text = value.toString(),
            style = AppTheme.typography.numericMd,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp),
        )
        AppIconButton(
            icon = Icons.Filled.Add,
            contentDescription = stringResource(R.string.app_stepper_increment),
            onClick = { onValueChange((value + 1).coerceAtMost(max)) },
            enabled = value < max,
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppStepper(value = 1, onValueChange = {})
    AppStepper(value = 12, onValueChange = {})
    AppStepper(value = 0, onValueChange = {}, min = 0)
}
