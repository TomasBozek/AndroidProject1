package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A continuous value that has no exact entry.
 *
 * **Never for a price.** A slider cannot land on an exact amount, and an amount that is nearly
 * right is wrong — use [AppTextField] with `numeric = true` for anything countable.
 */
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    valueLabel: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) {
            AppText(text = if (valueLabel != null) "$label · $valueLabel" else label, role = TextRole.Label)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.confirm.bg,
                activeTrackColor = AppTheme.colors.confirm.bg,
                inactiveTrackColor = AppTheme.colors.surfaceSunken,
            ),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSlider(value = 0.4f, onValueChange = {}, label = "Screen brightness", valueLabel = "40 %")
}
