package com.example.androidproject1.feature.trips.presentation.tripwizard

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.trips.presentation.destinationpicker.DestinationPickerDestination
import com.example.androidproject1.service.core.ui.component.Screen
import com.example.androidproject1.service.core.ui.navigation.NavResultEffect
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object TripWizardDestination : NavKey

/** The key the picker writes to. A constant, because only the wizard asks for a destination this way. */
const val TRIP_WIZARD_PICK_RESULT = "trip_wizard_picked_destination"

fun EntryProviderScope<NavKey>.tripWizardDestination(backStack: NavBackStack<NavKey>) {
    entry<TripWizardDestination> {
        val viewModel: TripWizardViewModel = koinViewModel()

        // Fires once per result, and consumes it, so returning to the wizard later does not
        // reapply a pick already made — core.4's requester half. See DestinationPickerDestination
        // for the responder half.
        NavResultEffect<String>(TRIP_WIZARD_PICK_RESULT) { destinationId ->
            viewModel.onDestinationPicked(destinationId)
        }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    TripWizardNavigation.PickDestination ->
                        backStack.add(DestinationPickerDestination(resultKey = TRIP_WIZARD_PICK_RESULT))

                    TripWizardNavigation.Saved, TripWizardNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            TripWizardScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
