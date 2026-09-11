package com.example.androidproject1.feature.trips.presentation.tripdetail

import com.example.androidproject1.feature.trips.domain.test.FakeTripsRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TripDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeTripsRepository = FakeTripsRepository()) = TripDetailViewModel(
        logger = FakeLogger(),
        args = TripDetailDestination(tripId = FakeTripsRepository.LISBON_TRIP.id),
        tripsRepository = repository,
    )

    @Test
    fun `loads the trip its route argument names`() = runTest {
        val state = viewModel().state.value

        assertEquals(FakeTripsRepository.LISBON_TRIP, state.data?.trip)
    }

    @Test
    fun `selecting a tab updates the state`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(TripDetailEvent.TabSelected(TripDetailState.TAB_BUDGET))

        assertEquals(TripDetailState.TAB_BUDGET, viewModel.state.value.data?.selectedTab)
    }

    @Test
    fun `delete raises a confirmation rather than deleting immediately`() = runTest {
        val repository = FakeTripsRepository()
        val viewModel = viewModel(repository)

        viewModel.onUiEvent(TripDetailEvent.DeleteClicked)

        assertNotNull(viewModel.state.value.alert)
        assertNull(repository.deletedId)
    }

    @Test
    fun `confirming the alert deletes the trip and navigates up`() = runTest {
        val repository = FakeTripsRepository()
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(TripDetailEvent.DeleteClicked)
        val alertId = viewModel.state.value.alert?.id.orEmpty()

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(id = alertId))

        assertEquals(FakeTripsRepository.LISBON_TRIP.id, repository.deletedId)
        assertEquals(TripDetailNavigation.NavigateUp, viewModel.navigation.first())
    }

    @Test
    fun `a trip that no longer exists navigates back up`() = runTest {
        val state = viewModel(FakeTripsRepository(initial = emptyList())).navigation.first()

        assertEquals(TripDetailNavigation.NavigateUp, state)
    }
}
