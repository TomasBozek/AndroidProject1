package com.example.androidproject1.feature.cart.domain

/**
 * @property price in minor units, as the catalog holds it — 450 is 4.50. Captured when the item
 * was added, so a later price change does not silently rewrite what the user agreed to.
 */
data class CartItem(
    val productId: String,
    val name: String,
    val price: Long,
    val quantity: Int,
) {

    val lineTotal: Long get() = price * quantity
}
