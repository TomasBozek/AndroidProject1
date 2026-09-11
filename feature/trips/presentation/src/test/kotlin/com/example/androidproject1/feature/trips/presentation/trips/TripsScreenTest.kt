package com.example.androidproject1.feature.trips.presentation.trips

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.androidproject1.core.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<TripsEvent>()

    private fun render(state: TripsState) {
        compose.setContent {
            AppTheme {
                TripsScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders the next trip card`() {
        render(TripsState.PREVIEW)

        compose.onNodeWithTag("trips_nextTripCard").assertIsDisplayed()
    }

    @Test
    fun `loading draws skeleton rows`() {
        render(TripsState.PREVIEW.copy(loading = true))

        compose.onNodeWithTag("trips_skeleton").assertIsDisplayed()
    }

    @Test
    fun `no trips shows the empty state instead of a card`() {
        render(TripsState.PREVIEW.copy(nextTrip = null))

        compose.onNodeWithTag("trips_emptyState").assertIsDisplayed()
    }

    @Test
    fun `the new-trip button reports its event`() {
        render(TripsState.PREVIEW)

        compose.onNodeWithTag("trips_newTripButton").performScrollTo().performClick()

        assertEquals(listOf(TripsEvent.NewTripClicked), events)
    }

    @Test
    fun `opening the next trip reports its event`() {
        render(TripsState.PREVIEW)

        compose.onNodeWithTag("trips_openNextTripButton").performScrollTo().performClick()

        assertEquals(listOf(TripsEvent.NextTripClicked), events)
    }
}
