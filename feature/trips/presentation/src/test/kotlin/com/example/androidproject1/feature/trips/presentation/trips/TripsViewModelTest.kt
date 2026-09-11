package com.example.androidproject1.feature.trips.presentation.trips

import com.example.androidproject1.feature.trips.domain.test.FakeTripsRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TripsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-09-11T00:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(repository: FakeTripsRepository = FakeTripsRepository()) =
        TripsViewModel(logger = FakeLogger(), tripsRepository = repository, clock = fixedClock)

    @Test
    fun `shows the count and the soonest upcoming trip`() = runTest {
        val state = viewModel().state.value

        assertEquals(false, state.data?.loading)
        assertEquals(1, state.data?.tripCount)
        assertEquals(FakeTripsRepository.LISBON_TRIP, state.data?.nextTrip)
    }

    @Test
    fun `an empty table has no next trip`() = runTest {
        val state = viewModel(FakeTripsRepository(initial = emptyList())).state.value

        assertNull(state.data?.nextTrip)
        assertEquals(0, state.data?.tripCount)
    }

    @Test
    fun `view all opens the list`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(TripsEvent.ViewAllClicked)

        assertEquals(TripsNavigation.ViewAll, viewModel.navigation.first())
    }

    @Test
    fun `the next-trip card opens its detail`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(TripsEvent.NextTripClicked)

        assertEquals(TripsNavigation.OpenTrip(FakeTripsRepository.LISBON_TRIP.id), viewModel.navigation.first())
    }
}
