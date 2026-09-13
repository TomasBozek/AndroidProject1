package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

/** One-off navigation intents, turned into back-stack calls in InventoryEditorDestination. */
sealed interface InventoryEditorNavigation {

    data object NavigateUp : InventoryEditorNavigation

    data object Saved : InventoryEditorNavigation
}
