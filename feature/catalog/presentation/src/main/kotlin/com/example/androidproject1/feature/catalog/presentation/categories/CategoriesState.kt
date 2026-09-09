package com.example.androidproject1.feature.catalog.presentation.categories

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.catalog.domain.Category

@Immutable
data class CategoriesState(
    val categories: List<Category>,
) {

    companion object {

        val PREVIEW = CategoriesState(
            categories = listOf(
                Category(id = "beverages", name = "Beverages"),
                Category(id = "bakery", name = "Bakery"),
                Category(id = "produce", name = "Produce"),
            ),
        )
    }
}
