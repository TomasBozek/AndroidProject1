package com.example.androidproject1.feature.cart.presentation.cart

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CartScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<CartEvent>()

    private fun render(state: CartState) {
        compose.setContent {
            AppTheme {
                CartScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `a cart with items shows the list and the checkout bar`() {
        render(CartState.PREVIEW)

        compose.onNodeWithTag("cart_itemList").assertIsDisplayed()
        compose.onNodeWithTag("cart_checkoutButton").assertIsDisplayed()
        compose.onAllNodesWithTag("cart_item")[0].assertIsDisplayed()
    }

    @Test
    fun `an empty cart shows the empty state instead of the list`() {
        render(CartState.EMPTY)

        compose.onNodeWithTag("cart_empty").assertIsDisplayed()
        // The checkout bar must not be reachable with nothing to check out.
        compose.onAllNodesWithTag("cart_checkoutButton").assertCountEquals(0)
    }

    @Test
    fun `removing an item reports it as an event`() {
        render(CartState.PREVIEW)

        compose.onAllNodesWithTag("cart_removeButton")[0].performClick()

        assertEquals(listOf(CartEvent.ItemRemoved("coffee")), events)
    }

    @Test
    fun `checkout reports it as an event rather than acting`() {
        render(CartState.PREVIEW)

        compose.onNodeWithTag("cart_checkoutButton").performClick()

        // The screen says what happened; the confirmation is the ViewModel's decision.
        assertEquals(listOf(CartEvent.CheckoutClicked), events)
    }

    @Test
    fun `the empty state offers a way to add something`() {
        render(CartState.EMPTY)

        compose.onNodeWithTag("cart_empty").assertIsDisplayed()

        assertEquals(emptyList<CartEvent>(), events)
    }
}
