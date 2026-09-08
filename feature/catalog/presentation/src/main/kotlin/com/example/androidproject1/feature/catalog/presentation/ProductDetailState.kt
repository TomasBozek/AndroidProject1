package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.feature.catalog.domain.Product

data class ProductDetailState(
    val product: Product,
) {

    companion object {

        val PREVIEW = ProductDetailState(
            product = Product(
                id = "coffee",
                categoryId = "beverages",
                name = "Coffee",
                price = 450,
                description = "Freshly ground, brewed to order.",
            ),
        )
    }
}
