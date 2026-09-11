package com.example.androidproject1.feature.trips.presentation.tripslist

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
class TripsListScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<TripsListEvent>()

    private fun render(state: TripsListState) {
        compose.setContent {
            AppTheme {
                TripsListScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders every trip it was given`() {
        render(TripsListState.PREVIEW)

        compose.onNodeWithTag("tripsList_list").assertIsDisplayed()
        compose.onAllNodesWithTag("tripsList_item")[0].assertIsDisplayed()
    }

    @Test
    fun `tapping a trip reports it as an event`() {
        render(TripsListState.PREVIEW)

        compose.onAllNodesWithTag("tripsList_item")[0].performClick()

        assertEquals(listOf(TripsListEvent.TripClicked(TripsListState.PREVIEW.trips[0])), events)
    }

    @Test
    fun `the new-trip fab reports its event`() {
        render(TripsListState.PREVIEW)

        compose.onNodeWithTag("tripsList_newTripButton").performClick()

        assertEquals(listOf(TripsListEvent.NewTripClicked), events)
    }

    @Test
    fun `loading draws skeleton rows instead of the list`() {
        render(TripsListState.PREVIEW.copy(loading = true))

        compose.onNodeWithTag("tripsList_skeleton").assertIsDisplayed()
    }

    @Test
    fun `the up arrow emits its navigation event`() {
        render(TripsListState.PREVIEW)

        compose.onNodeWithTag("tripsList_upButton").performClick()

        assertEquals(listOf(TripsListEvent.NavigateUpClicked), events)
    }
}
