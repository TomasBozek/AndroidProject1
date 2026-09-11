package com.example.androidproject1.feature.trips.presentation.trips

/** One-off navigation intents, turned into back-stack calls in TripsDestination. */
sealed interface TripsNavigation {

    data object ViewAll : TripsNavigation

    data object NewTrip : TripsNavigation

    data class OpenTrip(val tripId: String) : TripsNavigation
}
