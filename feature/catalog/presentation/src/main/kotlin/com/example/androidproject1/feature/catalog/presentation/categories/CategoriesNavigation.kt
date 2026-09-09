package com.example.androidproject1.feature.catalog.presentation.categories

/** One-off navigation intents, turned into back-stack calls in CategoriesDestination. */
sealed interface CategoriesNavigation {

    data class Products(val categoryId: String, val categoryName: String) : CategoriesNavigation
}
