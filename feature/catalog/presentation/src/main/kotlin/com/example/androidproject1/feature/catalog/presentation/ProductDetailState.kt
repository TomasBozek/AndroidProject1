package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.feature.catalog.domain.Product

data class ProductDetailState(
    // Null covers the (mock-data-only) case of a product that no longer exists.
    val product: Product?,
) {

    companion object {

        val PREVIEW = ProductDetailState(
            product = Product(
                id = "coffee",
                categoryId = "beverages",
                name = "Coffee",
                price = 4.50,
                description = "Freshly ground, brewed to order.",
            ),
        )
    }
}
