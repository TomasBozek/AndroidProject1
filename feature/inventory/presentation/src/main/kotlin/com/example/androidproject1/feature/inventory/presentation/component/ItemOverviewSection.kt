package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.InventoryDetailState
import com.example.androidproject1.service.core.ui.format.LocalFormats

/** The detail's first section: the item's facts, label left and value right. */
@Composable
fun ItemOverviewSection(
    item: Item,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    AppDescriptionList(
        rows = listOf(
            DescriptionRow(
                label = stringResource(R.string.inventory_editor_category_label),
                value = stringResource(item.category.labelRes()),
            ),
            DescriptionRow(
                label = stringResource(R.string.inventory_editor_condition_label),
                value = stringResource(item.condition.labelRes()),
            ),
            DescriptionRow(
                label = stringResource(R.string.inventory_editor_quantity_label),
                value = formats.quantity(item.quantity.toLong()),
                numeric = true,
            ),
            DescriptionRow(
                label = stringResource(R.string.inventory_editor_price_label),
                value = formats.money(item.priceMinor),
                numeric = true,
            ),
            DescriptionRow(
                label = stringResource(R.string.inventory_editor_insured_label),
                value = stringResource(
                    if (item.insured) R.string.inventory_editor_yes else R.string.inventory_editor_no,
                ),
            ),
        ),
        modifier = modifier.padding(horizontal = AppTheme.spacing.inset.lg),
    )
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemOverviewSection(item = InventoryDetailState.PREVIEW.item!!)
}
