package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * How large a control is, for every control that comes in more than one size.
 *
 * One scale rather than an enum per component: the design's document gives the field, the icon
 * button, the switch, the checkbox and the segmented control the same sm / md / lg vocabulary, and
 * a screen that has chosen `Small` for one of them means the same thing by it everywhere.
 *
 * [Small] sits below the 48 dp touch minimum and exists only where there is a mouse. On glass the
 * range starts at [Medium]. A button reads the same three names through its own metrics, which are
 * taller — see `AppButton` (D51).
 */
enum class ControlSize { Small, Medium, Large }

/**
 * The height a control of this size gets.
 *
 * [ControlSize.Medium] is the density's own minimum rather than a fixed 48 dp, so a till still
 * gets its 56 and a device with a mouse its 40 without any screen having to ask for a different
 * size. The other two are fixed: asking for [ControlSize.Small] is asking for the dense one on
 * purpose, and it is the one case where a control may be smaller than the density's floor.
 */
val ControlSize.height: Dp
    @Composable get() = when (this) {
        ControlSize.Small -> 40.dp
        ControlSize.Medium -> AppTheme.density.minTouchTarget
        ControlSize.Large -> 56.dp
    }

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // The scale is only visible on something that uses it, so the preview is three of the same
    // control rather than three empty boxes.
    AppTextField(value = "40", onValueChange = {}, size = ControlSize.Small, numeric = true)
    AppTextField(value = "48", onValueChange = {}, size = ControlSize.Medium, numeric = true)
    AppTextField(value = "56", onValueChange = {}, size = ControlSize.Large, numeric = true)
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm)) {
        AppSegmented(
            options = listOf("Cash", "Card"),
            selectedIndex = 0,
            onSelect = {},
            size = ControlSize.Small,
            modifier = Modifier,
        )
    }
}
