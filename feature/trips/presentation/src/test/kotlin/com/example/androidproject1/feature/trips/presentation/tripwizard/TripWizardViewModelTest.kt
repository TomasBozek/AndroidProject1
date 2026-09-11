package com.example.androidproject1.feature.trips.presentation.tripwizard

import com.example.androidproject1.feature.trips.domain.TripType
import com.example.androidproject1.feature.trips.domain.test.FakeDestinationsRepository
import com.example.androidproject1.feature.trips.domain.test.FakeTripsRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.form.ALERT_ID_DISCARD
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TripWizardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trips = FakeTripsRepository(initial = emptyList())
    private val destinations = FakeDestinationsRepository()

    private fun viewModel() = TripWizardViewModel(
        logger = FakeLogger(),
        tripsRepository = trips,
        destinationsRepository = destinations,
    )

    private fun TripWizardViewModel.fillDetails() {
        onUiEvent(TripWizardEvent.NameChanged("Summer in Lisbon"))
        onUiEvent(TripWizardEvent.StartDateChanged(LocalDate.of(2026, 7, 10)))
        onUiEvent(TripWizardEvent.EndDateChanged(LocalDate.of(2026, 7, 17)))
    }

    @Test
    fun `the first step cannot continue until it has a name and both dates`() {
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.data?.canContinue == false)

        viewModel.fillDetails()

        assertTrue(viewModel.state.value.data?.canContinue == true)
    }

    @Test
    fun `next moves to the destination step once the first step is valid`() {
        val viewModel = viewModel()
        viewModel.fillDetails()

        viewModel.onUiEvent(TripWizardEvent.NextClicked)

        assertEquals(TripWizardState.STEP_DESTINATION, viewModel.state.value.data?.step)
    }

    @Test
    fun `the destination step cannot continue until one is picked`() = runTest {
        val viewModel = viewModel()
        viewModel.fillDetails()
        viewModel.onUiEvent(TripWizardEvent.NextClicked)

        assertTrue(viewModel.state.value.data?.canContinue == false)

        viewModel.onDestinationPicked(FakeDestinationsRepository.LISBON.id)

        assertEquals(FakeDestinationsRepository.LISBON.name, viewModel.state.value.data?.destinationName)
        assertTrue(viewModel.state.value.data?.canContinue == true)
    }

    @Test
    fun `back on the first step leaves the wizard`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(TripWizardEvent.NavigateUpClicked)

        assertEquals(TripWizardNavigation.NavigateUp, viewModel.navigation.first())
    }

    @Test
    fun `back past the first step returns to the previous one instead of leaving`() {
        val viewModel = viewModel()
        viewModel.fillDetails()
        viewModel.onUiEvent(TripWizardEvent.NextClicked)

        viewModel.onUiEvent(TripWizardEvent.NavigateUpClicked)

        assertEquals(TripWizardState.STEP_DETAILS, viewModel.state.value.data?.step)
    }

    @Test
    fun `leaving a dirty first step by gesture asks before discarding`() {
        val viewModel = viewModel()
        viewModel.onUiEvent(TripWizardEvent.NameChanged("Something typed"))

        viewModel.onUiEvent(TripWizardEvent.BackRequested)

        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun `confirming the discard alert leaves the wizard`() = runTest {
        val viewModel = viewModel()
        viewModel.onUiEvent(TripWizardEvent.BackRequested)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(id = ALERT_ID_DISCARD))

        assertNull(viewModel.state.value.alert)
        assertEquals(TripWizardNavigation.NavigateUp, viewModel.navigation.first())
    }

    @Test
    fun `saving on the last step writes the trip and leaves`() = runTest {
        val viewModel = viewModel()
        viewModel.fillDetails()
        viewModel.onUiEvent(TripWizardEvent.NextClicked)
        viewModel.onDestinationPicked(FakeDestinationsRepository.LISBON.id)
        viewModel.onUiEvent(TripWizardEvent.NextClicked)

        viewModel.onUiEvent(TripWizardEvent.NextClicked)

        val saved = trips.saved
        assertEquals("Summer in Lisbon", saved?.name)
        assertEquals(FakeDestinationsRepository.LISBON.id, saved?.destinationId)
        assertEquals(TripType.Leisure, saved?.type)
        assertEquals(TripWizardNavigation.Saved, viewModel.navigation.first())
    }
}
