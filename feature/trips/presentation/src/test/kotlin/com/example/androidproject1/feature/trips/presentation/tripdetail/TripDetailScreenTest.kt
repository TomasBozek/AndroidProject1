package com.example.androidproject1.feature.trips.presentation.tripdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripDetailScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<TripDetailEvent>()

    private fun render(state: TripDetailState) {
        compose.setContent {
            AppTheme {
                TripDetailScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the trip it was given`() {
        render(TripDetailState.PREVIEW)

        compose.onNodeWithTag("tripDetail_tabs").assertIsDisplayed()
    }

    @Test
    fun `while loading, shows skeleton rows instead of the tabs`() {
        render(TripDetailState.PREVIEW.copy(trip = null))

        compose.onNodeWithTag("tripDetail_skeleton").assertIsDisplayed()
    }

    @Test
    fun `the delete menu item reports its event`() {
        // AppMenu draws its rows in a window of its own with no per-item testTag — see the
        // component's own OverlayScreenshotTest coverage — so this finds the destructive row by
        // the label the screen gave it rather than by a tag that does not exist.
        render(TripDetailState.PREVIEW)
        compose.onNodeWithTag("tripDetail_menuButton").performClick()

        compose.onNodeWithText("Delete trip").performClick()

        assertEquals(listOf(TripDetailEvent.DeleteClicked), events)
    }

    @Test
    fun `the up arrow emits its navigation event`() {
        render(TripDetailState.PREVIEW)

        compose.onNodeWithTag("tripDetail_upButton").performClick()

        assertEquals(listOf(TripDetailEvent.NavigateUpClicked), events)
    }
}
