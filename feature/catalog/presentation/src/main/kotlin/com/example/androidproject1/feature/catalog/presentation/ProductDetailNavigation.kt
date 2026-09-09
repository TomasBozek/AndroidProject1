package com.example.androidproject1.feature.catalog.presentation

sealed interface ProductDetailNavigation {

    /**
     * The user asked for [productId] to go in the cart.
     *
     * A navigation intent rather than a repository call: the catalog does not depend on the cart,
     * so it says what happened and `AppNavHost` decides what that means. The same reason the cart
     * does not know how to open the catalog.
     */
    data class AddToCart(val productId: String) : ProductDetailNavigation
}
