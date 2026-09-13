package com.example.androidproject1.feature.inventory.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppFormField
import com.example.androidproject1.core.ui.component.AppSlider
import com.example.androidproject1.core.ui.component.AppStepper
import com.example.androidproject1.core.ui.component.AppSwitch
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.inventory.presentation.R
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorEvent
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorState
import com.example.androidproject1.service.core.ui.format.LocalFormats

/** The editor's second step: how many, what it is worth, and whether it is insured. */
@Composable
fun ItemQuantityStep(
    state: InventoryEditorState,
    onEvent: (InventoryEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.md),
    ) {
        AppFormField(
            label = stringResource(R.string.inventory_editor_quantity_label),
            required = true,
            errorText = if (state.quantity > 0) null else stringResource(R.string.inventory_editor_quantity_error),
            modifier = Modifier.testTag("inventoryEditor_quantityField"),
        ) {
            AppStepper(
                value = state.quantity,
                onValueChange = { onEvent(InventoryEditorEvent.QuantityChanged(it)) },
                min = 0,
                max = MAX_QUANTITY,
            )
        }

        AppSlider(
            value = state.priceFraction,
            onValueChange = { onEvent(InventoryEditorEvent.PriceChanged(it)) },
            label = stringResource(R.string.inventory_editor_price_label),
            valueLabel = formats.money(state.priceMinor),
            modifier = Modifier.testTag("inventoryEditor_priceField"),
        )

        AppSwitch(
            checked = state.insured,
            onCheckedChange = { onEvent(InventoryEditorEvent.InsuredChanged(it)) },
            label = stringResource(R.string.inventory_editor_insured_label),
            supporting = stringResource(R.string.inventory_editor_insured_supporting),
            modifier = Modifier.testTag("inventoryEditor_insuredSwitch"),
        )
    }
}

private const val MAX_QUANTITY = 99

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    ItemQuantityStep(state = InventoryEditorState.PREVIEW, onEvent = {})
}
