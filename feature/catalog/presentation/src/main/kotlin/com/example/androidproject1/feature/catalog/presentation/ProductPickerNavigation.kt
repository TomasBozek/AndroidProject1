package com.example.androidproject1.feature.catalog.presentation

/** One-off navigation intents. Turned into back-stack calls in `AppNavHost`. */
sealed interface ProductPickerNavigation {

    /**
     * The user chose [productId]; hand it back to whoever asked and close.
     *
     * The picker does not know who asked — the requester\'s key came in as a route argument — which
     * is what lets one picker serve the cart today and anything else tomorrow.
     */
    data class Picked(val productId: String) : ProductPickerNavigation
}
