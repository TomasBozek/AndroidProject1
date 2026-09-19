package com.example.androidproject1.feature.home.presentation.home

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
 * What is on screen, and what a tap does — the half `HomeViewModelTest` cannot reach.
 *
 * The one thing worth asserting twice here is the empty branch: an empty favourites list is a
 * section of this screen and not the screen failing to load, so it must render *instead of the
 * list* and leave the greeting alone. `Screen()` would replace both.
 *
 * **Everything is found by `testTag`, never by text.**
 */
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<HomeEvent>()

    private fun render(state: HomeState) {
        compose.setContent {
            AppTheme {
                HomeScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders a row per favourite`() {
        render(HomeState.PREVIEW)

        compose.onNodeWithTag("HomeScreen").assertIsDisplayed()
        compose.onNodeWithTag("home_favouritesList").assertIsDisplayed()
        compose.onAllNodesWithTag("home_favouriteItem")
            .assertCountEquals(HomeState.PREVIEW.favourites.size)
    }

    @Test
    fun `no favourites shows the empty state instead of the list`() {
        render(HomeState.PREVIEW.copy(favourites = emptyList()))

        compose.onNodeWithTag("home_favouritesEmpty").assertIsDisplayed()
        compose.onNodeWithTag("home_favouritesList").assertDoesNotExist()
    }

    @Test
    fun `the empty state leaves the rest of the screen standing`() {
        // Not a ContentState: the greeting is not part of what failed to load.
        render(HomeState.PREVIEW.copy(favourites = emptyList()))

        compose.onNodeWithTag("HomeScreen").assertIsDisplayed()
    }

    @Test
    fun `the inventory card is there, and its button reports the event`() {
        render(HomeState.PREVIEW)

        compose.onNodeWithTag("home_inventoryCard").assertIsDisplayed()
        compose.onNodeWithTag("home_inventoryButton").performClick()

        assertEquals(listOf(HomeEvent.InventoryClicked), events)
    }

    @Test
    fun `the movies card is there, and its button reports the event`() {
        render(HomeState.PREVIEW)

        compose.onNodeWithTag("home_moviesCard").assertIsDisplayed()
        compose.onNodeWithTag("home_moviesButton").performClick()

        assertEquals(listOf(HomeEvent.MoviesClicked), events)
    }

    @Test
    fun `removing a favourite reports it as an event`() {
        render(HomeState.PREVIEW)

        compose.onAllNodesWithTag("home_favouriteRemoveButton")[0].performClick()

        assertEquals(
            listOf(HomeEvent.FavouriteRemoved(HomeState.PREVIEW.favourites[0].id)),
            events,
        )
    }
}
