package com.example.androidproject1.feature.trips.presentation.tripwizard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
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
class TripWizardScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<TripWizardEvent>()

    private fun render(state: TripWizardState) {
        compose.setContent {
            AppTheme {
                TripWizardScreen(state = state, onEvent = events::add)
            }
        }
    }

    @Test
    fun `the first step renders its fields and the step indicator`() {
        render(TripWizardState())

        compose.onNodeWithTag("tripWizard_stepProgress").assertIsDisplayed()
        compose.onNodeWithTag("tripWizard_nameField").assertIsDisplayed()
    }

    @Test
    fun `typing a name reports it as an event`() {
        render(TripWizardState())

        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("tripWizard_nameField")))
            .performTextInput("Lisbon")

        assertEquals(listOf(TripWizardEvent.NameChanged("Lisbon")), events)
    }

    @Test
    fun `next is disabled until the first step is valid`() {
        render(TripWizardState())

        compose.onNodeWithTag("tripWizard_nextButton").assertIsNotEnabled()
    }

    @Test
    fun `the destination step shows its choose-destination button`() {
        render(TripWizardState.PREVIEW.copy(step = TripWizardState.STEP_DESTINATION))

        compose.onNodeWithTag("tripWizard_chooseDestinationButton").assertIsDisplayed()
    }

    @Test
    fun `tapping choose destination reports its event`() {
        render(TripWizardState.PREVIEW.copy(step = TripWizardState.STEP_DESTINATION))

        compose.onNodeWithTag("tripWizard_chooseDestinationButton").performClick()

        assertEquals(listOf(TripWizardEvent.PickDestinationClicked), events)
    }

    @Test
    fun `the review step shows the budget slider`() {
        render(
            TripWizardState.PREVIEW.copy(
                step = TripWizardState.STEP_REVIEW,
                destinationId = "lisbon",
                destinationName = "Lisbon, Portugal",
            ),
        )

        compose.onNodeWithTag("tripWizard_budgetSlider").assertIsDisplayed()
    }

    @Test
    fun `the up arrow emits its navigation event`() {
        render(TripWizardState())

        compose.onNodeWithTag("tripWizard_upButton").performClick()

        assertEquals(listOf(TripWizardEvent.NavigateUpClicked), events)
    }
}
