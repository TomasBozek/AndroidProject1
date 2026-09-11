package com.example.androidproject1.feature.trips.presentation.tripslist

import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.feature.trips.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import java.time.Clock
import java.time.LocalDate

class TripsListViewModel(
    logger: Logger,
    private val tripsRepository: TripsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : BaseViewModel<TripsListState, TripsListEvent, TripsListNavigation>(
    // The shell (top bar, FAB) draws immediately; the rows are skeletons until the first load
    // lands — see TripsListState.loading. The shared overlay would only sit over a screen that
    // already has plenty to show.
    initialState = TripsListState(today = LocalDate.now(clock), loading = true),
    logger = logger.withTag("TripsListViewModel"),
) {

    init {
        observeTrips()
    }

    override fun onUiEvent(event: TripsListEvent) {
        when (event) {
            is TripsListEvent.TripClicked -> navigate(TripsListNavigation.OpenTrip(event.trip.id))
            TripsListEvent.NewTripClicked -> navigate(TripsListNavigation.NewTrip)
            TripsListEvent.NavigateUpClicked -> navigate(TripsListNavigation.NavigateUp)
        }
    }

    private fun observeTrips() = observe(
        flow = { tripsRepository.observeTrips() },
        errorDisplay = ErrorDisplay.Inline,
        onData = { trips ->
            updateData { copy(trips = trips, today = LocalDate.now(clock), loading = false) }
            if (trips.isEmpty()) {
                showContent(ContentState.Empty(message = R.string.trips_list_empty.toUiText()))
            } else {
                clearContent()
            }
        },
    )
}
