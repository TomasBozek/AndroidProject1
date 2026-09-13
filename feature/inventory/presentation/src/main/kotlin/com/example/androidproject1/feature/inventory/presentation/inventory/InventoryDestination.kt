package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object InventoryDestination : NavKey

fun EntryProviderScope<NavKey>.inventoryDestination(backStack: NavBackStack<NavKey>) {
    entry<InventoryDestination> {
        val viewModel: InventoryViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            // The detail and the editor arrive with E3S2 and E3S3; until then a tap has nowhere to
            // go, and saying so here is better than a branch that pushes a key that does not exist.
            onNavigation = { navigation ->
                when (navigation) {
                    is InventoryNavigation.OpenItem -> Unit
                    InventoryNavigation.NewItem -> Unit
                }
            },
        ) { state, onEvent ->
            InventoryScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
