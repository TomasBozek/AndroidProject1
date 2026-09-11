package com.example.androidproject1.feature.trips.presentation.destinationpicker

import com.example.androidproject1.feature.trips.domain.test.FakeDestinationsRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DestinationPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeDestinationsRepository = FakeDestinationsRepository()) =
        DestinationPickerViewModel(
            logger = FakeLogger(),
            // The route key the destination hands in. It carries the requester's result key, so
            // the picker survives process death still knowing who asked.
            args = DestinationPickerDestination(resultKey = "trip_wizard_picked_destination"),
            destinationsRepository = repository,
        )

    @Test
    fun `shows everything the repository has`() = runTest {
        val state = viewModel().state.value

        assertEquals(
            listOf(FakeDestinationsRepository.LISBON, FakeDestinationsRepository.PRAGUE),
            state.data?.destinations,
        )
    }

    @Test
    fun `the result key comes from the route, not from state`() = runTest {
        assertEquals("trip_wizard_picked_destination", viewModel().state.value.data?.resultKey)
    }

    @Test
    fun `picking a destination reports its id and nothing else`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(DestinationPickerEvent.DestinationClicked(FakeDestinationsRepository.LISBON))

        assertEquals(
            DestinationPickerNavigation.Picked(FakeDestinationsRepository.LISBON.id),
            viewModel.navigation.first(),
        )
    }

    @Test
    fun `the up arrow leaves without picking`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(DestinationPickerEvent.NavigateUpClicked)

        assertEquals(DestinationPickerNavigation.NavigateUp, viewModel.navigation.first())
    }
}
