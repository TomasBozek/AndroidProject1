package com.example.androidproject1.feature.catalog.presentation.component

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.component.AppText
import com.example.androidproject1.core.ui.component.TextRole
import com.example.androidproject1.core.ui.format.LocalFormats
import com.example.androidproject1.feature.catalog.domain.Product

/** What a search matched: the product and what it costs. */
@Composable
fun SearchResultList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    LazyColumn(modifier = modifier.testTag("productSearch_resultList")) {
        items(products, key = { it.id }) { product ->
            AppListItem(
                headline = product.name,
                onClick = { onProductClick(product) },
                modifier = Modifier.testTag("productSearch_resultItem"),
                // A price is numeric, so it gets tabular figures and lines up down the column.
                trailing = { AppText(text = formats.money(product.price), role = TextRole.Numeric) },
            )
            AppDivider()
        }
    }
}

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    SearchResultList(
        products = listOf(
            Product(
                id = "coffee",
                categoryId = "beverages",
                name = "Coffee",
                price = 450,
                description = "Freshly ground, brewed to order.",
            ),
            Product(
                id = "tea",
                categoryId = "beverages",
                name = "Tea",
                price = 380,
                description = "Loose leaf, three minutes.",
            ),
        ),
        onProductClick = {},
    )
}
