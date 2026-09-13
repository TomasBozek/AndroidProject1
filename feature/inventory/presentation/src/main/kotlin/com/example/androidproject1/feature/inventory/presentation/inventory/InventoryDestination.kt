package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.androidproject1.feature.inventory.presentation.inventorydetail.InventoryDetailDestination
import com.example.androidproject1.feature.inventory.presentation.inventoryeditor.InventoryEditorDestination
import com.example.androidproject1.service.core.ui.component.Screen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Serializable
data object InventoryDestination : NavKey

fun EntryProviderScope<NavKey>.inventoryDestination(backStack: NavBackStack<NavKey>) {
    entry<InventoryDestination> {
        val viewModel: InventoryViewModel = koinViewModel()

        Screen(
            viewModel = viewModel,
            onNavigation = { navigation ->
                when (navigation) {
                    is InventoryNavigation.OpenItem -> backStack.add(InventoryDetailDestination(navigation.itemId))
                    // A new item is a new id, minted here: the editor edits whatever id it is given
                    // and starts blank when nothing has that id.
                    InventoryNavigation.NewItem -> backStack.add(
                        InventoryEditorDestination(UUID.randomUUID().toString()),
                    )
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
