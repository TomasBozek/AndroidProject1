package com.example.androidproject1.feature.trips.presentation.trips

import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripStatus
import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TripsViewModel(
    logger: Logger,
    private val tripsRepository: TripsRepository,
    private val clock: Clock,
) : BaseViewModel<TripsState, TripsEvent, TripsNavigation>(
    // The shell draws immediately; the stats and the next-trip card are skeletons until the
    // first load lands.
    initialState = TripsState(today = LocalDate.now(clock)),
    logger = logger.withTag("TripsViewModel"),
) {

    init {
        observeTrips()
    }

    override fun onUiEvent(event: TripsEvent) {
        when (event) {
            TripsEvent.ViewAllClicked -> navigate(TripsNavigation.ViewAll)
            TripsEvent.NewTripClicked -> navigate(TripsNavigation.NewTrip)
            TripsEvent.NextTripClicked -> {
                val tripId = uiState.value.data?.nextTrip?.id ?: return
                navigate(TripsNavigation.OpenTrip(tripId))
            }
        }
    }

    private fun observeTrips() = observe(
        flow = { tripsRepository.observeTrips() },
        errorDisplay = ErrorDisplay.Inline,
        onData = { trips ->
            val today = LocalDate.now(clock)
            val next = trips
                .filter { it.status(today) != TripStatus.Completed }
                .minByOrNull { it.startDate }
            updateData {
                copy(
                    tripCount = trips.size,
                    nextTrip = next,
                    today = today,
                    countdownFraction = next?.let { countdownFraction(it, today) } ?: 0f,
                    loading = false,
                )
            }
        },
    )

    private fun countdownFraction(trip: Trip, today: LocalDate): Float {
        val daysUntil = ChronoUnit.DAYS.between(today, trip.startDate).coerceAtLeast(0)
        val remaining = daysUntil.toFloat() / TripsState.PLANNING_HORIZON_DAYS
        return (1f - remaining).coerceIn(0f, 1f)
    }
}
