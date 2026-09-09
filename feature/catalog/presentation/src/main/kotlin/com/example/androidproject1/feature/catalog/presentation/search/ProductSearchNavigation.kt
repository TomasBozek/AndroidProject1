package com.example.androidproject1.feature.catalog.presentation.search

/** One-off navigation intents, turned into back-stack calls in ProductSearchDestination. */
sealed interface ProductSearchNavigation {

    data class ProductDetail(val productId: String) : ProductSearchNavigation

    data object NavigateUp : ProductSearchNavigation
}
