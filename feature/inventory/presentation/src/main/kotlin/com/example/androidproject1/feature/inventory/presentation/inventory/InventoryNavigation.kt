package com.example.androidproject1.feature.inventory.presentation.inventory

/** One-off navigation intents, turned into back-stack calls in InventoryDestination. */
sealed interface InventoryNavigation {

    data class OpenItem(val itemId: String) : InventoryNavigation

    data object NewItem : InventoryNavigation
}
