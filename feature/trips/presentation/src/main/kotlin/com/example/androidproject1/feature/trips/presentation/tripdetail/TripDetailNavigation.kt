package com.example.androidproject1.feature.trips.presentation.tripdetail

/** One-off navigation intents, turned into back-stack calls in TripDetailDestination. */
sealed interface TripDetailNavigation {

    /** Left, whether by the up arrow or after the trip was deleted. */
    data object NavigateUp : TripDetailNavigation
}
