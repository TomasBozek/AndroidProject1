package com.example.androidproject1.feature.catalog.presentation.categories

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

/**
 * What is on screen, and what a tap does — the half `CategoriesViewModelTest` cannot reach.
 *
 * **Everything is found by `testTag`, never by text.** The category names come from the data and
 * get translated; the tags do not.
 */
@RunWith(RobolectricTestRunner::class)
class CategoriesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<CategoriesEvent>()

    private fun render(state: CategoriesState) {
        compose.setContent {
            AppTheme {
                CategoriesScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders a row per category`() {
        render(CategoriesState.PREVIEW)

        compose.onNodeWithTag("CategoriesScreen").assertIsDisplayed()
        compose.onNodeWithTag("categories_list").assertIsDisplayed()
        compose.onAllNodesWithTag("categories_item")
            .assertCountEquals(CategoriesState.PREVIEW.categories.size)
    }

    @Test
    fun `an empty list still renders the screen`() {
        // The tab root has no empty state of its own: what must not happen is the screen
        // disappearing with the data.
        render(CategoriesState(categories = emptyList()))

        compose.onNodeWithTag("CategoriesScreen").assertIsDisplayed()
        compose.onAllNodesWithTag("categories_item").assertCountEquals(0)
    }

    @Test
    fun `tapping a category reports it as an event`() {
        render(CategoriesState.PREVIEW)

        compose.onAllNodesWithTag("categories_item")[0].performClick()

        assertEquals(
            listOf(CategoriesEvent.CategoryClicked(CategoriesState.PREVIEW.categories[0])),
            events,
        )
    }
}
