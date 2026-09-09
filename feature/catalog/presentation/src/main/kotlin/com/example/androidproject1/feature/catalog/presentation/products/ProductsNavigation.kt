package com.example.androidproject1.feature.catalog.presentation.products

/** One-off navigation intents, turned into back-stack calls in ProductsDestination. */
sealed interface ProductsNavigation {

    data class ProductDetail(val productId: String) : ProductsNavigation
}
