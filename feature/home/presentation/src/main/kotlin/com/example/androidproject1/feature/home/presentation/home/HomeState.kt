package com.example.androidproject1.feature.home.presentation.home

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.home.presentation.R
import com.example.androidproject1.service.core.ui.text.UiText
import com.example.androidproject1.service.core.ui.text.toUiText

@Immutable
data class HomeState(
    // UiText rather than String: the ViewModel picks the text without a Context.
    val greeting: UiText,
    val favourites: List<Product> = emptyList(),
    /** How many items the inventory holds — the card's number, and its reason to exist. */
    val inventoryCount: Int = 0,
) {

    companion object {

        val PREVIEW = HomeState(
            greeting = R.string.home_greeting.toUiText(),
            inventoryCount = 12,
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
