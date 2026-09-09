package com.example.androidproject1.feature.cart.presentation.cart

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.text.UiText
import com.example.androidproject1.core.ui.text.toPluralUiText
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.presentation.R

@Immutable
data class CartState(
    val items: List<CartItem> = emptyList(),
) {

    val total: Long get() = items.sumOf { it.lineTotal }

    val itemCount: Int get() = items.sumOf { it.quantity }

    val isEmpty: Boolean get() = items.isEmpty()

    /**
     * "1 item" / "2 items", and in Czech four forms rather than two (D24).
     *
     * The first argument picks the form and the second fills the `%d`. They are the same number
     * here, which is exactly why `toPluralUiText` is named differently from `toUiText` — passing
     * one where two are meant is a mistake the type system cannot catch.
     */
    val itemCountLabel: UiText
        get() = R.plurals.cart_item_count.toPluralUiText(itemCount, itemCount)

    companion object {

        val PREVIEW = CartState(
            items = listOf(
                CartItem(productId = "coffee", name = "Coffee", price = 450, quantity = 2),
                CartItem(productId = "croissant", name = "Croissant", price = 275, quantity = 1),
            ),
        )

        /** Where the layout is most likely to be wrong, so it is a golden of its own. */
        val EMPTY = CartState()
    }
}
