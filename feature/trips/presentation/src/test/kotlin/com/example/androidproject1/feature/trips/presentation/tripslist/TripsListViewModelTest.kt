package com.example.androidproject1.feature.trips.presentation.tripslist

import com.example.androidproject1.feature.trips.domain.test.FakeTripsRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TripsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-09-11T00:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(repository: FakeTripsRepository = FakeTripsRepository()) =
        TripsListViewModel(logger = FakeLogger(), tripsRepository = repository, clock = fixedClock)

    @Test
    fun `shows every trip the repository has, no longer loading`() = runTest {
        val state = viewModel().state.value

        assertEquals(false, state.data?.loading)
        assertEquals(listOf(FakeTripsRepository.LISBON_TRIP), state.data?.trips)
    }

    @Test
    fun `an empty table shows the empty content state`() = runTest {
        val state = viewModel(FakeTripsRepository(initial = emptyList())).state.value

        assertTrue(state.content is ContentState.Empty)
        assertEquals(emptyList<Any>(), state.data?.trips)
    }

    @Test
    fun `tapping a trip reports which one`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(TripsListEvent.TripClicked(FakeTripsRepository.LISBON_TRIP))

        assertEquals(
            TripsListNavigation.OpenTrip(FakeTripsRepository.LISBON_TRIP.id),
            viewModel.navigation.first(),
        )
    }

    @Test
    fun `the new-trip button opens the wizard`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(TripsListEvent.NewTripClicked)

        assertEquals(TripsListNavigation.NewTrip, viewModel.navigation.first())
    }
}
