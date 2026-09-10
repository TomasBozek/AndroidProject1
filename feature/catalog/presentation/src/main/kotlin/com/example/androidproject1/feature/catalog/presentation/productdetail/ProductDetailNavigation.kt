package com.example.androidproject1.feature.catalog.presentation.productdetail

/**
 * One-off navigation intents, turned into back-stack calls in `ProductDetailDestination`.
 *
 * Adding to the cart used to be one, because the catalog does not depend on the cart's
 * presentation — but it does not have to: the cart's `AddProductToCart` is a domain type, which
 * any feature may use, and the write belongs in the ViewModel's own scope rather than the nav
 * host's composition.
 */
sealed interface ProductDetailNavigation {

    data object NavigateUp : ProductDetailNavigation
}
