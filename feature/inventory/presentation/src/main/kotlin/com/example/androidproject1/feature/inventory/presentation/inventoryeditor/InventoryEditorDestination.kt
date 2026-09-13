package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A route that carries the item's id. A new item is a fresh id the list minted, so create and
 * edit are one destination and one ViewModel.
 */
@Serializable
data class InventoryEditorDestination(val itemId: String) : NavKey

fun EntryProviderScope<NavKey>.inventoryEditorDestination(backStack: NavBackStack<NavKey>) {
    entry<InventoryEditorDestination> { key ->
        // The route key goes straight into the ViewModel through Koin, so it is available in
        // `init` and comes back with the entry after process death — neither of which is true of
        // a LaunchedEffect that calls a load() function.
        val viewModel: InventoryEditorViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    InventoryEditorNavigation.NavigateUp -> backStack.removeLastOrNull()
                    // The list and the detail both observe the table, so there is nothing to hand back.
                    InventoryEditorNavigation.Saved -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            InventoryEditorScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
