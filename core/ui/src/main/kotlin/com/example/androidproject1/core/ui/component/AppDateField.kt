package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * A date, chosen from the platform's own picker.
 *
 * **Never a text field.** A typed date is a format argument nobody wins — `01/02` is January in one
 * country and February in the next — so the field shows the value and opens a picker, and the only
 * thing that can come out of it is a real date.
 *
 * The field is read-only by design: [value] is what the caller holds, and the picker is the only
 * way it changes. That is also why there is no `errorText` for a malformed date; there is no way
 * to produce one.
 *
 * The rendering of [value] is deliberately plain here. Formatting is a system role — `core.10`
 * gives `Formats.date` — and this switches to it the day that lands rather than growing a
 * formatter of its own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateField(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Choose a date",
    enabled: Boolean = true,
    size: ControlSize = ControlSize.Medium,
) {
    var open by remember { mutableStateOf(false) }
    PickerField(
        text = value?.toString(),
        placeholder = placeholder,
        label = label,
        enabled = enabled,
        size = size,
        icon = Icons.Filled.DateRange,
        onClick = { open = true },
        modifier = modifier,
    )

    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli(),
        )
        AppDialog(
            title = label ?: placeholder,
            onDismiss = { open = false },
            content = { DatePicker(state = state) },
            actions = {
                AppButton(label = "Cancel", onClick = { open = false }, kind = ButtonKind.Ghost)
                AppButton(
                    label = "Choose",
                    onClick = {
                        // The picker works in UTC midnights, so the date is read back in the same
                        // zone it was written in — anything else is off by one for half the world.
                        state.selectedDateMillis?.let {
                            onValueChange(
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
                        }
                        open = false
                    },
                )
            },
        )
    }
}

/**
 * A time of day, chosen from the platform's own picker.
 *
 * The same rule as [AppDateField]: what a person types is ambiguous and what a picker returns is
 * not. The picker follows the device's 12- or 24-hour setting, which is the one place that choice
 * belongs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimeField(
    value: LocalTime?,
    onValueChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Choose a time",
    enabled: Boolean = true,
    size: ControlSize = ControlSize.Medium,
) {
    var open by remember { mutableStateOf(false) }
    PickerField(
        text = value?.let { "%02d:%02d".format(it.hour, it.minute) },
        placeholder = placeholder,
        label = label,
        enabled = enabled,
        size = size,
        // The core Material set ships no clock glyph and D33 keeps that set, so the time
        // field wears the same chevron `AppSelect` does: this opens a chooser.
        icon = Icons.Filled.KeyboardArrowDown,
        onClick = { open = true },
        modifier = modifier,
    )

    if (open) {
        val state = rememberTimePickerState(
            initialHour = value?.hour ?: 0,
            initialMinute = value?.minute ?: 0,
        )
        AppDialog(
            title = label ?: placeholder,
            onDismiss = { open = false },
            content = { TimePicker(state = state) },
            actions = {
                AppButton(label = "Cancel", onClick = { open = false }, kind = ButtonKind.Ghost)
                AppButton(
                    label = "Choose",
                    onClick = {
                        onValueChange(LocalTime.of(state.hour, state.minute))
                        open = false
                    },
                )
            },
        )
    }
}

/**
 * The half the two fields share: the sunken frame of an [AppTextField] with no text input in it.
 *
 * It is deliberately not `AppTextField(enabled = false)` — a disabled field says "not now", and
 * this one is very much available, it just does not take typing.
 */
@Composable
private fun PickerField(
    text: String?,
    placeholder: String,
    label: String?,
    enabled: Boolean,
    size: ControlSize,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
    ) {
        if (label != null) {
            AppText(text = label, role = TextRole.Label, color = colors.textSecondary)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = size.height)
                .clip(AppTheme.shapes.md)
                .background(if (enabled) colors.surfaceSunken else colors.surfaceBase)
                .border(1.dp, colors.borderStrong, AppTheme.shapes.md)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = AppTheme.spacing.inset.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
        ) {
            AppText(
                text = text ?: placeholder,
                role = if (text == null) TextRole.Secondary else TextRole.Body,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) colors.textSecondary else colors.textDisabled,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppDateField(value = LocalDate.of(2026, 9, 9), onValueChange = {}, label = "Delivery")
    AppDateField(value = null, onValueChange = {}, label = "Closed until")
    AppTimeField(value = LocalTime.of(19, 24), onValueChange = {}, label = "Opened")
    AppTimeField(value = null, onValueChange = {}, label = "Last order", enabled = false)
}
