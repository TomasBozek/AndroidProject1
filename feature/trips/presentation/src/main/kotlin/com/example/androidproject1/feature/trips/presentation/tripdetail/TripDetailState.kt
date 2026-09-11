package com.example.androidproject1.feature.trips.presentation.tripdetail

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripType
import java.time.LocalDate

@Immutable
data class TripDetailState(
    /** The route argument. Always present, unlike [trip] — see the ViewModel. */
    val tripId: String,
    val trip: Trip? = null,
    val selectedTab: Int = TAB_ITINERARY,
    val today: LocalDate = LocalDate.now(),
) {

    companion object {

        const val TAB_ITINERARY = 0
        const val TAB_NOTES = 1
        const val TAB_BUDGET = 2

        val PREVIEW = TripDetailState(
            tripId = "trip-lisbon",
            today = LocalDate.of(2026, 9, 11),
            trip = Trip(
                id = "trip-lisbon",
                name = "Summer in Lisbon",
                destinationId = "lisbon",
                destinationName = "Lisbon, Portugal",
                type = TripType.Leisure,
                startDate = LocalDate.of(2026, 7, 10),
                endDate = LocalDate.of(2026, 7, 17),
                travelers = 2,
                budgetMinMinor = 4_000_00,
                budgetMaxMinor = 8_000_00,
                notes = "Book the tram tour and the fado dinner before the flight sells out.",
            ),
        )
    }
}
