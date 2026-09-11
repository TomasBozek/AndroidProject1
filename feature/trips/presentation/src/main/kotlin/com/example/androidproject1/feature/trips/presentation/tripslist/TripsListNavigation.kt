package com.example.androidproject1.feature.trips.presentation.tripslist

/** One-off navigation intents, turned into back-stack calls in TripsListDestination. */
sealed interface TripsListNavigation {

    data class OpenTrip(val tripId: String) : TripsListNavigation

    data object NewTrip : TripsListNavigation

    data object NavigateUp : TripsListNavigation
}
