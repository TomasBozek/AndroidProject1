package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * A sunken field: the ground it sits on is lower than the surface around it, which is the whole
 * signal that it takes input.
 *
 * The four states are rest, focus, error and disabled. Focus is a ring in `colors.focusRing` and is
 * never suppressed — a till is driven from a keyboard as much as from the glass. **An error always
 * carries [errorText]**, never colour alone: red on its own is invisible to a good share of the
 * people using it.
 *
 * Set [numeric] for money and quantities; it asks for the number keyboard and turns on tabular
 * figures, so digits stop shifting under the caret.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    numeric: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val isError = errorText != null

    val borderColor = when {
        !enabled -> colors.border
        isError -> colors.destructive.bg
        focused -> colors.focusRing
        else -> colors.borderStrong
    }
    val textColor = if (enabled) colors.textPrimary else colors.textDisabled

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) {
            Text(text = label, style = AppTheme.typography.labelMd, color = colors.textSecondary)
        }

        val ring = if (focused || isError) 3.dp else 1.dp
        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = colors.focusRing,
                backgroundColor = colors.focusRing.copy(alpha = 0.28f),
            ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                interactionSource = interaction,
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
                ),
                textStyle = (if (numeric) AppTheme.typography.numericMd else AppTheme.typography.bodyMd)
                    .copy(color = textColor),
                cursorBrush = SolidColor(colors.focusRing),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                    .clip(AppTheme.shapes.md)
                    .background(if (enabled) colors.surfaceSunken else colors.surfaceBase)
                    .border(ring, borderColor, AppTheme.shapes.md)
                    .padding(horizontal = AppTheme.spacing.inset.md, vertical = 10.dp),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = AppTheme.typography.bodyMd,
                                color = colors.textTertiary,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        val supporting = errorText ?: helperText
        if (supporting != null) {
            Text(
                text = supporting,
                style = AppTheme.typography.labelMd,
                color = if (isError) colors.destructive.bg else colors.textSecondary,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppTextField(value = "", onValueChange = {}, label = "Search", placeholder = "Product name")
    AppTextField(value = "Pilsner", onValueChange = {}, label = "Item", helperText = "From the catalogue")
    AppTextField(value = "12", onValueChange = {}, label = "Quantity", numeric = true)
    AppTextField(
        value = "abc",
        onValueChange = {},
        label = "Code",
        errorText = "No product with this code",
    )
    AppTextField(value = "Locked", onValueChange = {}, label = "Till", enabled = false)
}
