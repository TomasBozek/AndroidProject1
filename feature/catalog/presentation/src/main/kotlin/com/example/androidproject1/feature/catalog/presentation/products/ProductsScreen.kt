package com.example.androidproject1.feature.catalog.presentation.products

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.feature.catalog.presentation.asPrice

@Composable
fun ProductsScreen(
    state: ProductsState,
    onEvent: (ProductsEvent) -> Unit,
) {
    AppScaffold(
        screenId = "ProductsScreen",
        topBar = { AppTopBar(title = state.categoryName) },
        contentPadding = false,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("products_list"),
        ) {
            items(state.products, key = { it.id }) { product ->
                AppListItem(
                    headline = product.name,
                    onClick = { onEvent(ProductsEvent.ProductClicked(product)) },
                    modifier = Modifier.testTag("products_item"),
                    // A price is numeric, so it gets tabular figures and lines up down the column.
                    trailing = { AppText(text = product.price.asPrice(), role = TextRole.Numeric) },
                )
                AppDivider()
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview() = ThemedScreenPreview {
    ProductsScreen(
        state = ProductsState.PREVIEW,
    ) {}
}
