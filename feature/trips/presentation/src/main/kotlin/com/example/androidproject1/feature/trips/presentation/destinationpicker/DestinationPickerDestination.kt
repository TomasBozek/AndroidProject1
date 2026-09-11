package com.example.androidproject1.feature.trips.presentation.destinationpicker

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import com.example.androidproject1.service.core.ui.navigation.rememberNavResultSender
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * @property resultKey where to put the chosen destination. Carried in the route rather than held
 * in a ViewModel, so it survives process death with the back stack entry it belongs to.
 */
@Serializable
data class DestinationPickerDestination(val resultKey: String) : NavKey

fun EntryProviderScope<NavKey>.destinationPickerDestination(backStack: NavBackStack<NavKey>) {
    entry<DestinationPickerDestination> { key ->
        val viewModel: DestinationPickerViewModel = koinViewModel { parametersOf(key) }
        val setNavResult = rememberNavResultSender(key.resultKey)

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is DestinationPickerNavigation.Picked -> {
                        // Set, then pop: the store lives above the entries, so the value outlives
                        // this screen leaving the back stack.
                        setNavResult(navigation.destinationId)
                        backStack.removeLastOrNull()
                    }

                    // No result set, so the requester sees nothing arrive.
                    DestinationPickerNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            DestinationPickerScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
