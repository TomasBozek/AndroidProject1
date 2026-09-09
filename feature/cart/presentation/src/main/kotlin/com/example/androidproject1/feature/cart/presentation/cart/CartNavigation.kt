package com.example.androidproject1.feature.cart.presentation.cart

/** One-off navigation intents. Turned into back-stack calls in `AppNavHost`. */
sealed interface CartNavigation {

    /**
     * Browse the catalog to pick something.
     *
     * The cart knows nothing about the catalog: `AppNavHost` decides what "pick a product" means,
     * which is what keeps one feature\'s presentation module out of another\'s.
     */
    data object PickProduct : CartNavigation
}
