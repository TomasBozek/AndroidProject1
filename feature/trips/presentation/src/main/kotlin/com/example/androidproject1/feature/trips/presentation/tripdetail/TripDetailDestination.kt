package com.example.androidproject1.feature.trips.presentation.tripdetail

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class TripDetailDestination(val tripId: String) : NavKey

fun EntryProviderScope<NavKey>.tripDetailDestination(backStack: NavBackStack<NavKey>) {
    entry<TripDetailDestination> { key ->
        val viewModel: TripDetailViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    TripDetailNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            TripDetailScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
