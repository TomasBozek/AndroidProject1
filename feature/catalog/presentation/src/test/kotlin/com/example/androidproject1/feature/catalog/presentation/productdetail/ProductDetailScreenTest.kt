package com.example.androidproject1.feature.catalog.presentation.productdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProductDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<ProductDetailEvent>()

    private fun render(state: ProductDetailState) {
        compose.setContent {
            AppTheme {
                ProductDetailScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the product and the heart`() {
        render(ProductDetailState.PREVIEW)

        compose.onNodeWithTag("productDetail_favouriteButton").assertIsDisplayed()
    }

    @Test
    fun `tapping the heart reports it as an event`() {
        render(ProductDetailState.PREVIEW)

        compose.onNodeWithTag("productDetail_favouriteButton").performClick()

        assertEquals(listOf(ProductDetailEvent.FavouriteToggled), events)
    }

    @Test
    fun `the heart reports the same event when it is already favourited`() {
        // The screen is stateless: it says what happened, not what should happen next. Both
        // states must raise the same event, or the ViewModel's toggle would depend on the view.
        render(ProductDetailState.PREVIEW.copy(isFavourite = true))

        compose.onNodeWithTag("productDetail_favouriteButton").performClick()

        assertEquals(listOf(ProductDetailEvent.FavouriteToggled), events)
    }

    @Test
    fun `the up arrow emits its navigation event`() {
        render(ProductDetailState.PREVIEW)

        compose.onNodeWithTag("productDetail_upButton").performClick()

        assertEquals(listOf(ProductDetailEvent.NavigateUpClicked), events)
    }
}
