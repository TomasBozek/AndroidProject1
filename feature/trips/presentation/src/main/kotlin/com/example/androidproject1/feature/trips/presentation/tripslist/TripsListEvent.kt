package com.example.androidproject1.feature.trips.presentation.tripslist

import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface TripsListEvent : UiEvent {

    data class TripClicked(val trip: Trip) : TripsListEvent

    data object NewTripClicked : TripsListEvent

    /** The Up arrow was tapped. Every non-root screen has one. */
    data object NavigateUpClicked : TripsListEvent
}
