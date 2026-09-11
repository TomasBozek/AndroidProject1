package com.example.androidproject1.feature.trips.presentation.trips

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.trips.presentation.tripdetail.TripDetailDestination
import com.example.androidproject1.feature.trips.presentation.tripslist.TripsListDestination
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object TripsDestination : NavKey

fun EntryProviderScope<NavKey>.tripsDestination(backStack: NavBackStack<NavKey>) {
    entry<TripsDestination> {
        val viewModel: TripsViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    TripsNavigation.ViewAll -> backStack.add(TripsListDestination)
                    TripsNavigation.NewTrip -> backStack.add(TripWizardDestination)
                    is TripsNavigation.OpenTrip -> backStack.add(TripDetailDestination(navigation.tripId))
                }
            },
        ) { state, onEvent ->
            TripsScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
