package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.feature.catalog.domain.Product

data class ProductsState(
    val categoryName: String,
    val products: List<Product>,
) {

    companion object {

        val PREVIEW = ProductsState(
            categoryName = "Beverages",
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
                    price = 300,
                    description = "A pot of loose-leaf tea.",
                ),
            ),
        )
    }
}
