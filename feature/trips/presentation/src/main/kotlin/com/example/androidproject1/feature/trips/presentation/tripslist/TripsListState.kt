package com.example.androidproject1.feature.trips.presentation.tripslist

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripType
import java.time.LocalDate

@Immutable
data class TripsListState(
    val trips: List<Trip> = emptyList(),
    /** So `TripStatus` can be read off each row without every row computing its own clock. */
    val today: LocalDate,
    /** The first load is in flight — drawn as skeleton rows rather than the shared overlay, since
     * there is already a real shell (the top bar, the FAB) to show it under. */
    val loading: Boolean = false,
) {

    companion object {

        val PREVIEW = TripsListState(
            today = LocalDate.of(2026, 9, 11),
            trips = listOf(
                Trip(
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
                    notes = "",
                ),
                Trip(
                    id = "trip-prague",
                    name = "Prague conference",
                    destinationId = "prague",
                    destinationName = "Prague, Czechia",
                    type = TripType.Business,
                    startDate = LocalDate.of(2026, 10, 2),
                    endDate = LocalDate.of(2026, 10, 5),
                    travelers = 1,
                    budgetMinMinor = 1_500_00,
                    budgetMaxMinor = 2_500_00,
                    notes = "",
                ),
            ),
        )
    }
}
