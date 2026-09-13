package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** A route that carries the item's id; the screen observes it, so an edit shows up without a reload. */
@Serializable
data class InventoryDetailDestination(val itemId: String) : NavKey

fun EntryProviderScope<NavKey>.inventoryDetailDestination(backStack: NavBackStack<NavKey>) {
    entry<InventoryDetailDestination> { key ->
        // The route key goes straight into the ViewModel through Koin, so it is available in
        // `init` and comes back with the entry after process death — neither of which is true of
        // a LaunchedEffect that calls a load() function.
        val viewModel: InventoryDetailViewModel = koinViewModel { parametersOf(key) }

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is InventoryDetailNavigation.Edit -> backStack.add(InventoryEditorDestination(navigation.itemId))
                    InventoryDetailNavigation.NavigateUp -> backStack.removeLastOrNull()
                }
            },
        ) { state, onEvent ->
            InventoryDetailScreen(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}
