package com.example.androidproject1.feature.catalog.presentation

/** One-off navigation intents, turned into navController calls in CategoriesDestination. */
sealed interface CategoriesNavigation {

    data class Products(val categoryId: String, val categoryName: String) : CategoriesNavigation
}
