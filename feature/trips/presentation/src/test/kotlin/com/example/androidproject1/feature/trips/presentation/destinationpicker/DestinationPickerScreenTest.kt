package com.example.androidproject1.feature.trips.presentation.destinationpicker

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
class DestinationPickerScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<DestinationPickerEvent>()

    private fun render(state: DestinationPickerState) {
        compose.setContent {
            AppTheme {
                DestinationPickerScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `renders every destination it was given`() {
        render(DestinationPickerState.PREVIEW)

        compose.onNodeWithTag("destinationPicker_list").assertIsDisplayed()
        compose.onAllNodesWithTag("destinationPicker_item")[0].assertIsDisplayed()
    }

    @Test
    fun `tapping a destination reports it as an event`() {
        render(DestinationPickerState.PREVIEW)

        compose.onAllNodesWithTag("destinationPicker_item")[0].performClick()

        assertEquals(
            listOf(DestinationPickerEvent.DestinationClicked(DestinationPickerState.PREVIEW.destinations[0])),
            events,
        )
    }

    @Test
    fun `the up arrow emits its navigation event`() {
        render(DestinationPickerState.PREVIEW)

        compose.onNodeWithTag("destinationPicker_upButton").performClick()

        assertEquals(listOf(DestinationPickerEvent.NavigateUpClicked), events)
    }
}
