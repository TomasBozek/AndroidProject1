package com.example.androidproject1.feature.cart.presentation.cart

import com.example.androidproject1.feature.cart.domain.CartItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The cart's "N items" in Czech, which is the whole reason feat.9 picked Czech.
 *
 * Czech has four CLDR plural categories where English has two, and a `values-cs` short of one
 * still builds: Android falls back to `other`, and the cart reads "2 položek" for the rest of the
 * product's life. `doctor.py` checks the four forms are declared; only a real resource table can
 * say which of them a count selects, so this runs under Robolectric with `qualifiers = "cs"`.
 *
 * `@Config(sdk = …)` is pinned for the reason every other Robolectric test here pins it:
 * Robolectric ships an SDK image per API level and has none for this project's `targetSdk`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "cs")
class CartItemCountCzechTest {

    private fun label(count: Int): String = CartState(
        items = listOf(CartItem(productId = "coffee", name = "Coffee", price = 450, quantity = count)),
    ).itemCountLabel.resolve(RuntimeEnvironment.getApplication())

    @Test
    fun `one takes the singular`() {
        assertEquals("1 položka", label(1))
    }

    @Test
    fun `two takes the few form, which English has no equivalent of`() {
        // The form an `one`/`other` translation gets wrong, and the one a reader notices first.
        assertEquals("2 položky", label(2))
    }

    @Test
    fun `four is still the few form`() {
        assertEquals("4 položky", label(4))
    }

    @Test
    fun `five takes the other form`() {
        assertEquals("5 položek", label(5))
    }

    @Test
    fun `zero takes the other form, not the singular`() {
        assertEquals("0 položek", label(0))
    }
}
