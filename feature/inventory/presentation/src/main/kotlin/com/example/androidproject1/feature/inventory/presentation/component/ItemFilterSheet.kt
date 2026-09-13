package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppButton
import com.example.androidproject1.core.ui.component.AppCheckbox
import com.example.androidproject1.core.ui.component.AppFormField
import com.example.androidproject1.core.ui.component.AppSelect
import com.example.androidproject1.core.ui.component.AppSheet
import com.example.androidproject1.core.ui.component.AppSlider
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.ButtonKind
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventory.InventoryEvent
import com.example.androidproject1.feature.inventory.presentation.inventory.InventoryState
import com.example.androidproject1.feature.inventory.presentation.inventory.InventoryViewModel
import com.example.androidproject1.service.core.ui.format.LocalFormats

/**
 * The filter, in a sheet: a category, the tags under a tri-state master, a price cap with a money
 * label and a Clear. The sheet draws in a window of its own, which is why `OverlayScreenshotTest`
 * in `:core:ui` records one of this shape at the narrowest phone — a preview cannot see it.
 */
@Composable
fun ItemFilterSheet(
    state: InventoryState,
    onEvent: (InventoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    val categories = listOf<ItemCategory?>(null) + ItemCategory.entries
    AppSheet(
        onDismiss = { onEvent(InventoryEvent.FilterDismissed) },
        title = stringResource(R.string.inventory_filter_title),
        modifier = modifier.testTag("inventory_filterSheet"),
    ) {
        AppFormField(
            label = stringResource(R.string.inventory_editor_category_label),
            modifier = Modifier.testTag("inventory_categoryField"),
        ) {
            AppSelect(
                options = categories.map { category ->
                    if (category ==
                        null
                    ) {
                        stringResource(R.string.inventory_filter_any_category)
                    } else {
                        stringResource(category.labelRes())
                    }
                },
                selectedIndex = categories.indexOf(state.filter.category),
                onSelect = { onEvent(InventoryEvent.FilterCategorySelected(categories[it])) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppFormField(
            label = stringResource(R.string.inventory_editor_tags_label),
            modifier = Modifier.testTag("inventory_tagsGroup"),
        ) {
            AppCheckbox(
                checked = state.allFilterTagsState,
                onCheckedChange = { onEvent(InventoryEvent.FilterAllTagsChanged(it)) },
                label = stringResource(R.string.inventory_editor_all_tags_label),
                modifier = Modifier.testTag("inventory_allTagsCheckbox"),
            )
            ItemTag.entries.forEach { tag ->
                AppCheckbox(
                    checked = if (tag in state.filter.tags) CheckState.On else CheckState.Off,
                    onCheckedChange = { onEvent(InventoryEvent.FilterTagChanged(tag, it)) },
                    label = stringResource(tag.labelRes()),
                    modifier = Modifier.padding(start = AppTheme.spacing.inset.lg),
                )
            }
        }

        val maxPrice = state.filter.maxPriceMinor
        AppSlider(
            value = InventoryViewModel.fractionFor(maxPrice),
            onValueChange = { onEvent(InventoryEvent.FilterMaxPriceChanged(it)) },
            label = stringResource(R.string.inventory_filter_max_price_label),
            valueLabel = if (maxPrice == null) {
                stringResource(R.string.inventory_filter_no_cap)
            } else {
                formats.money(maxPrice)
            },
            modifier = Modifier.testTag("inventory_maxPriceField"),
        )

        if (state.filter.isEmpty) {
            AppText(text = stringResource(R.string.inventory_filter_none_active), role = TextRole.Secondary)
        }
        AppButton(
            label = stringResource(R.string.inventory_filter_clear),
            onClick = { onEvent(InventoryEvent.FilterCleared) },
            kind = ButtonKind.Outline,
            enabled = !state.filter.isEmpty,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventory_clearButton"),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    // A sheet draws in its own window; this shows nothing a preview can capture. See the KDoc.
    AppText(
        text = "ItemFilterSheet draws in a window of its own — see OverlayScreenshotTest.",
        role = TextRole.Secondary,
    )
}
