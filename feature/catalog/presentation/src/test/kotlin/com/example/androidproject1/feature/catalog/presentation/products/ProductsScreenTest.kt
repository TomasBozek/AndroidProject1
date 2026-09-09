package com.example.androidproject1.feature.catalog.presentation.products

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
import org.robolectric.annotation.Config

/**
 * What is on screen, and what a tap does — the half `ProductsViewModelTest` cannot reach.
 *
 * **Everything is found by `testTag`, never by text.** The product names and the category in the
 * top bar are data, and the prices are formatted per locale; none of them is a way to reach a row.
 */
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
// Robolectric ships an SDK image per API level and has none for this project's targetSdk, so the
// level is pinned to the newest it does have. Raise it when Robolectric catches up; nothing in a
// screen test depends on the difference.
@Config(sdk = [ROBOLECTRIC_SDK])
class ProductsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<ProductsEvent>()

    private fun render(state: ProductsState) {
        compose.setContent {
            AppTheme {
                ProductsScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders a row per product`() {
        render(ProductsState.PREVIEW)

        compose.onNodeWithTag("ProductsScreen").assertIsDisplayed()
        compose.onNodeWithTag("products_list").assertIsDisplayed()
        compose.onAllNodesWithTag("products_item")
            .assertCountEquals(ProductsState.PREVIEW.products.size)
    }

    @Test
    fun `an empty category renders without collapsing`() {
        render(ProductsState.PREVIEW.copy(products = emptyList()))

        compose.onNodeWithTag("ProductsScreen").assertIsDisplayed()
        compose.onAllNodesWithTag("products_item").assertCountEquals(0)
    }

    @Test
    fun `tapping a product reports it as an event`() {
        render(ProductsState.PREVIEW)

        compose.onAllNodesWithTag("products_item")[0].performClick()

        assertEquals(
            listOf(ProductsEvent.ProductClicked(ProductsState.PREVIEW.products[0])),
            events,
        )
    }
}
