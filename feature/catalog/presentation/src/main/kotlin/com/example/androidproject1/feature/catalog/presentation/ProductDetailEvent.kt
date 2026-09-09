package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface ProductDetailEvent : UiEvent {

    /** The heart was tapped. What it means is the ViewModel's business, not the screen's. */
    data object FavouriteToggled : ProductDetailEvent

    /** Add this product to the cart. What "the cart" is stays outside this module. */
    data object AddToCartClicked : ProductDetailEvent
}
