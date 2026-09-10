package com.example.androidproject1.feature.catalog.presentation.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProductSearchScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<ProductSearchEvent>()

    private fun render(state: ProductSearchState) {
        compose.setContent {
            AppTheme {
                ProductSearchScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `a searched query shows its results`() {
        render(ProductSearchState.PREVIEW)

        compose.onNodeWithTag("productSearch_queryField").assertIsDisplayed()
        compose.onNodeWithTag("productSearch_resultList").assertIsDisplayed()
    }

    @Test
    fun `a query that matched nothing shows an empty state, not an empty list`() {
        render(ProductSearchState.PREVIEW.copy(results = emptyList()))

        compose.onNodeWithTag("productSearch_resultsEmpty").assertIsDisplayed()
        compose.onNodeWithTag("productSearch_resultList").assertDoesNotExist()
    }

    @Test
    fun `a field nobody has typed in shows the recent searches instead`() {
        render(ProductSearchState.EMPTY.copy(recents = listOf("coffee")))

        compose.onNodeWithTag("productSearch_recentList").assertIsDisplayed()
        compose.onNodeWithTag("productSearch_resultList").assertDoesNotExist()
    }

    @Test
    fun `a first run shows the recents empty state`() {
        render(ProductSearchState.EMPTY)

        compose.onNodeWithTag("productSearch_recentsEmpty").assertIsDisplayed()
    }

    @Test
    fun `typing reports every keystroke`() {
        render(ProductSearchState.EMPTY)

        // A compound component is tagged on its group, so the input inside it is reached by the
        // action it offers rather than by a second tag.
        compose.onNode(
            hasSetTextAction() and hasAnyAncestor(hasTestTag("productSearch_queryField")),
        ).performTextInput("c")

        assertEquals(listOf(ProductSearchEvent.QueryChanged("c")), events)
    }

    @Test
    fun `tapping a recent search reports it`() {
        render(ProductSearchState.EMPTY.copy(recents = listOf("coffee")))

        compose.onAllNodesWithTag("productSearch_recentItem")[0].performClick()

        assertEquals(listOf(ProductSearchEvent.RecentClicked("coffee")), events)
    }

    @Test
    fun `tapping a result reports the product`() {
        val state = ProductSearchState.PREVIEW
        render(state)

        compose.onAllNodesWithTag("productSearch_resultItem")[0].performClick()

        assertEquals(listOf(ProductSearchEvent.ProductClicked(state.results.first())), events)
    }
}
