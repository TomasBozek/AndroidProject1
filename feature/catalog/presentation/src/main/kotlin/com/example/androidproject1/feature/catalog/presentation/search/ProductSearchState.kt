package com.example.androidproject1.feature.catalog.presentation.search

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * @property query what is in the field right now — echoed straight back, so typing is never
 * behind the debounce.
 * @property results what the last debounced query matched. Empty while [query] is blank, which is
 * not the same as "no matches" — hence [searched].
 * @property searched whether the results below belong to a real query. Without it a blank field
 * and a query that matched nothing look identical, and one of them deserves an empty state.
 */
@Immutable
data class ProductSearchState(
    val query: String,
    val results: List<Product>,
    val recents: List<String>,
    val searched: Boolean,
) {

    companion object {

        val PREVIEW = ProductSearchState(
            query = "cof",
            results = listOf(
                Product(
                    id = "coffee",
                    categoryId = "beverages",
                    name = "Coffee",
                    price = 450,
                    description = "Freshly ground, brewed to order.",
                ),
            ),
            recents = listOf("coffee", "tea"),
            searched = true,
        )

        val EMPTY = ProductSearchState(
            query = "",
            results = emptyList(),
            recents = emptyList(),
            searched = false,
        )
    }
}

/** The states this screen is drawn in: results, a query that matched nothing, and a fresh field. */
class ProductSearchStatePreviews : PreviewParameterProvider<ProductSearchState> {

    override val values = sequenceOf(
        ProductSearchState.PREVIEW,
        ProductSearchState.PREVIEW.copy(query = "zzz", results = emptyList()),
        ProductSearchState.EMPTY.copy(recents = listOf("coffee", "a rather long previous search")),
    )
}
