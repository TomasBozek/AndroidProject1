package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
 * figures, so digits stop shifting under the caret. [password] masks the value and asks for the
 * password keyboard; [keyboardType] overrides the choice when neither fits, as an email does.
 *
 * [suffix] is the unit a number is in — `kg`, `%`, `Kc`. It is drawn inside the field rather than
 * after it, so the unit travels with the value and cannot be read as the label of the next field,
 * and it is never part of [value]: what the caller gets back is the number alone.
 *
 * Set [frame] to `false` for a field inside an [AppFieldGroup], which draws the frame for the whole
 * group. A framed field inside a framed group is a box in a box.
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
    keyboardType: KeyboardType? = null,
    password: Boolean = false,
    suffix: String? = null,
    size: ControlSize = ControlSize.Medium,
    frame: Boolean = true,
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
            // Drawn for the eye only. The same string is on the input's own semantics node below,
            // so leaving this one readable would have a screen reader announce the label twice.
            Text(
                text = label,
                style = AppTheme.typography.labelMd,
                color = colors.textSecondary,
                modifier = Modifier.clearAndSetSemantics { },
            )
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
                visualTransformation = if (password) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType ?: when {
                        password -> KeyboardType.Password
                        numeric -> KeyboardType.Number
                        else -> KeyboardType.Text
                    },
                ),
                textStyle = (if (numeric) AppTheme.typography.numericMd else AppTheme.typography.bodyMd)
                    .copy(color = textColor),
                cursorBrush = SolidColor(colors.focusRing),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = size.height)
                    .then(
                        if (frame) {
                            Modifier
                                .clip(AppTheme.shapes.md)
                                .background(
                                    if (enabled) colors.surfaceSunken else colors.surfaceBase,
                                )
                                .border(ring, borderColor, AppTheme.shapes.md)
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = AppTheme.spacing.inset.md, vertical = 10.dp)
                    // The label belongs to the input, not to a sibling above it: a `Text` beside
                    // the field is a separate node, so a screen reader reaching the input read out
                    // an unnamed edit box. `text` rather than `contentDescription` because the
                    // value lives in `editableText` and the two do not collide.
                    .semantics { if (label != null) text = AnnotatedString(label) },
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = AppTheme.typography.bodyMd,
                                    color = colors.textSecondary,
                                )
                            }
                            inner()
                        }
                        if (suffix != null) {
                            Text(
                                text = suffix,
                                style = AppTheme.typography.labelMd,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(start = AppTheme.spacing.inline.sm),
                            )
                        }
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
    AppTextField(value = "hunter2", onValueChange = {}, label = "Password", password = true)
    AppTextField(
        value = "0,420",
        onValueChange = {},
        label = "Weight",
        numeric = true,
        suffix = "kg",
    )
    AppTextField(value = "Small", onValueChange = {}, size = ControlSize.Small)
    AppTextField(value = "Large", onValueChange = {}, size = ControlSize.Large)
}
