package com.example.androidproject1.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.theme.AppTheme

/**
 * One choice from more than a handful.
 *
 * Below five options prefer [AppRadio] or [AppSegmented] — both show every option without a second
 * tap. The options open in an [AppMenu], so the list is not a platform popup with its own styling.
 */
@Composable
fun AppSelect(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = AppTheme.colors
    val selected = options.getOrNull(selectedIndex)
    AppMenu(
        expanded = expanded,
        onDismiss = { expanded = false },
        items = options.mapIndexed { index, option ->
            MenuItem(label = option) {
                onSelect(index)
                expanded = false
            }
        },
        anchor = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AppTheme.density.minTouchTarget)
                    .clip(AppTheme.shapes.md)
                    .background(colors.surfaceSunken)
                    .border(1.dp, colors.borderStrong, AppTheme.shapes.md)
                    .clickable(enabled = enabled, role = Role.DropdownList) { expanded = true }
                    .padding(horizontal = AppTheme.spacing.inset.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inline.sm),
            ) {
                AppText(
                    text = selected ?: placeholder,
                    role = if (selected == null) TextRole.Secondary else TextRole.Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    AppSelect(
        options = listOf("Cash", "Card", "Voucher", "Invoice", "Split"),
        selectedIndex = 1,
        onSelect = {},
    )
    AppSelect(
        options = listOf("A", "B"),
        selectedIndex = -1,
        onSelect = {},
        placeholder = "Choose a price list",
    )
}
