package com.example.androidproject1.feature.home.presentation.home

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.home.presentation.R

@Immutable
data class HomeState(
    // UiText rather than String: the ViewModel picks the text without a Context.
    val greeting: UiText,
    val favourites: List<Product> = emptyList(),
) {

    companion object {

        val PREVIEW = HomeState(
            greeting = R.string.home_greeting.toUiText(),
            favourites = listOf(
                Product(
                    id = "coffee",
                    categoryId = "beverages",
                    name = "Coffee",
                    price = 450,
                    description = "Freshly ground, brewed to order.",
                ),
                Product(
                    id = "croissant",
                    categoryId = "bakery",
                    name = "Croissant",
                    price = 275,
                    description = "Buttery, baked fresh every morning.",
                ),
            ),
        )
    }
}
