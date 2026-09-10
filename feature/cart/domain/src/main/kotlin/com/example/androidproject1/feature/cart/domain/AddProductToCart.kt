package com.example.androidproject1.feature.cart.domain

import com.example.androidproject1.service.core.domain.result.Outcome

/**
 * Adds one of a product to the cart.
 *
 * A use case rather than a bare [CartRepository] call, because the caller is another feature:
 * product detail knows a product and nothing about how a cart line is shaped, and that the first
 * add is a quantity of one is the cart's rule to hold, not the catalog's.
 *
 * It runs wherever the caller runs it, which is the point — this used to be launched in the nav
 * host's composition scope, and a rotation just after the tap cancelled the write.
 */
class AddProductToCart(private val cartRepository: CartRepository) {

    suspend operator fun invoke(productId: String, name: String, price: Long): Outcome<Unit> =
        cartRepository.add(
            CartItem(productId = productId, name = name, price = price, quantity = 1),
        )
}
