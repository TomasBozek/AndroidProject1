package com.example.androidproject1.feature.trips.presentation.tripslist

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.trips.presentation.tripdetail.TripDetailDestination
import com.example.androidproject1.feature.trips.presentation.tripwizard.TripWizardDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object TripsListDestination : NavKey

fun EntryProviderScope<NavKey>.tripsListDestination(backStack: NavBackStack<NavKey>) {
    entry<TripsListDestination> {
        val viewModel: TripsListViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is TripsListNavigation.OpenTrip ->
                        backStack.add(TripDetailDestination(tripId = navigation.tripId))

                    TripsListNavigation.NewTrip -> backStack.add(TripWizardDestination)
                    TripsListNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            TripsListScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
