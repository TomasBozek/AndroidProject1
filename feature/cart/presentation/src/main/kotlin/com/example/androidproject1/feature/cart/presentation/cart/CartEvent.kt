package com.example.androidproject1.feature.cart.presentation.cart

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface CartEvent : UiEvent {

    data class QuantityChanged(val productId: String, val quantity: Int) : CartEvent

    data class ItemRemoved(val productId: String) : CartEvent

    /** Opens the catalog in picker mode; the chosen product comes back through `core.4`. */
    data object AddItemClicked : CartEvent

    data object CheckoutClicked : CartEvent
}
