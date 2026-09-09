package com.example.androidproject1.feature.catalog.presentation.productpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.format.LocalFormats
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.catalog.presentation.R

@Composable
fun ProductPickerScreen(
    state: ProductPickerState,
    onEvent: (ProductPickerEvent) -> Unit,
) {
    AppScaffold(
        screenId = "ProductPickerScreen",
        topBar = { AppTopBar(title = stringResource(R.string.product_picker_title)) },
    ) {
        val formats = LocalFormats.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("productPicker_list"),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.stack.xs),
        ) {
            items(items = state.products, key = { it.id }) { product ->
                AppListItem(
                    headline = product.name,
                    supporting = formats.money(product.price),
                    onClick = { onEvent(ProductPickerEvent.ProductClicked(product)) },
                    modifier = Modifier.testTag("productPicker_item"),
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    ProductPickerScreen(state = ProductPickerState.PREVIEW) {}
}
