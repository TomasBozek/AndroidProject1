package com.example.androidproject1.feature.trips.presentation.trips

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripType
import java.time.LocalDate

@Immutable
data class TripsState(
    val tripCount: Int = 0,
    val nextTrip: Trip? = null,
    val today: LocalDate = LocalDate.now(),
    /** How close the next trip is, 0 at [PLANNING_HORIZON_DAYS] out and 1 on the day it starts. */
    val countdownFraction: Float = 0f,
    val loading: Boolean = true,
) {

    companion object {

        const val PLANNING_HORIZON_DAYS = 30L

        val PREVIEW = TripsState(
            tripCount = 3,
            today = LocalDate.of(2026, 9, 11),
            countdownFraction = 0.7f,
            loading = false,
            nextTrip = Trip(
                id = "trip-lisbon",
                name = "Summer in Lisbon",
                destinationId = "lisbon",
                destinationName = "Lisbon, Portugal",
                type = TripType.Leisure,
                startDate = LocalDate.of(2026, 9, 20),
                endDate = LocalDate.of(2026, 9, 27),
                travelers = 2,
                budgetMinMinor = 4_000_00,
                budgetMaxMinor = 8_000_00,
                notes = "",
            ),
        )
    }
}

/** For the preview, and so for `PreviewScreenshotTest`'s goldens: loaded, loading, and empty. */
class TripsStatePreviews : PreviewParameterProvider<TripsState> {

    override val values = sequenceOf(
        TripsState.PREVIEW,
        TripsState.PREVIEW.copy(loading = true),
        TripsState.PREVIEW.copy(nextTrip = null, tripCount = 0),
    )
}
