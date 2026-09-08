package com.example.androidproject1.feature.catalog.presentation

/** One-off navigation intents, turned into navController calls in ProductsDestination. */
sealed interface ProductsNavigation {

    data class ProductDetail(val productId: String) : ProductsNavigation
}
