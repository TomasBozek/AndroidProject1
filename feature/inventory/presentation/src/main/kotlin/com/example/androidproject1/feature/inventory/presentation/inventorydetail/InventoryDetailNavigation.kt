package com.example.androidproject1.feature.inventory.presentation.inventorydetail

/** One-off navigation intents, turned into back-stack calls in InventoryDetailDestination. */
sealed interface InventoryDetailNavigation {

    data class Edit(val itemId: String) : InventoryDetailNavigation

    data object NavigateUp : InventoryDetailNavigation
}
