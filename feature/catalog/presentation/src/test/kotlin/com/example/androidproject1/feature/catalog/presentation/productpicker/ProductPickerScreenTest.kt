package com.example.androidproject1.feature.catalog.presentation.productpicker

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
class ProductPickerScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<ProductPickerEvent>()

    private fun render(state: ProductPickerState) {
        compose.setContent {
            AppTheme {
                ProductPickerScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders every product it was given`() {
        render(ProductPickerState.PREVIEW)

        compose.onNodeWithTag("productPicker_list").assertIsDisplayed()
        compose.onAllNodesWithTag("productPicker_item")[0].assertIsDisplayed()
    }

    @Test
    fun `tapping a product reports it as an event`() {
        render(ProductPickerState.PREVIEW)

        compose.onAllNodesWithTag("productPicker_item")[0].performClick()

        assertEquals(
            listOf(ProductPickerEvent.ProductClicked(ProductPickerState.PREVIEW.products[0])),
            events,
        )
    }

    @Test
    fun `an empty catalog renders without collapsing`() {
        render(ProductPickerState())

        compose.onNodeWithTag("productPicker_list").assertIsDisplayed()
    }
}
