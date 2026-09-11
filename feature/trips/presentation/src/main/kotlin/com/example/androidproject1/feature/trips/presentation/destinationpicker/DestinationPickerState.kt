package com.example.androidproject1.feature.trips.presentation.destinationpicker

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.trips.domain.Destination

@Immutable
data class DestinationPickerState(
    /** Where to put the chosen destination. Carried in the route, not held here — see the ViewModel. */
    val resultKey: String,
    val destinations: List<Destination> = emptyList(),
) {

    companion object {

        val PREVIEW = DestinationPickerState(
            resultKey = "trip_wizard_pick_destination",
            destinations = listOf(
                Destination("lisbon", "Lisbon", "Portugal", "Hills, trams and the river Tagus."),
                Destination("prague", "Prague", "Czechia", "A skyline of spires over the Vltava."),
                Destination("kyoto", "Kyoto", "Japan", "Temples, gardens and a thousand gates."),
            ),
        )
    }
}
