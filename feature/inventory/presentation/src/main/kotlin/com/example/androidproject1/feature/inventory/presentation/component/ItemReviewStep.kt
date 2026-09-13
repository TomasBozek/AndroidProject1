package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppCard
import com.example.androidproject1.core.ui.component.AppDescriptionList
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.DescriptionRow
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorState
import com.example.androidproject1.service.core.ui.format.LocalFormats

/** The editor's last step: everything the three before it collected, read back before saving. */
@Composable
fun ItemReviewStep(
    state: InventoryEditorState,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    // Resolved up here: a string resource is read in composition, not inside a `joinToString`.
    val tagLabels = state.tags.map { stringResource(it.labelRes()) }
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventoryEditor_reviewTile"),
    ) {
        AppText(text = state.name.value, role = TextRole.Title)
        AppDescriptionList(
            rows = listOf(
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_category_label),
                    value = stringResource(state.category.labelRes()),
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_condition_label),
                    value = stringResource(state.condition.labelRes()),
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_acquired_label),
                    value = state.acquiredOn?.let(formats::date),
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_quantity_label),
                    value = formats.quantity(state.quantity.toLong()),
                    numeric = true,
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_price_label),
                    value = formats.money(state.priceMinor),
                    numeric = true,
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_insured_label),
                    value = stringResource(
                        if (state.insured) R.string.inventory_editor_yes else R.string.inventory_editor_no,
                    ),
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_tags_label),
                    value = tagLabels.takeIf { it.isNotEmpty() }?.joinToString(),
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_owner_label),
                    value = state.owner,
                ),
                DescriptionRow(
                    label = stringResource(R.string.inventory_editor_notes_label),
                    value = state.notes.ifBlank { null },
                ),
            ),
        )
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemReviewStep(state = InventoryEditorState.PREVIEW)
}
