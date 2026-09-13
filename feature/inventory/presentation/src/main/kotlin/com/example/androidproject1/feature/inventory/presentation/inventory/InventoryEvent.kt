package com.example.androidproject1.feature.inventory.presentation.inventory

import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface InventoryEvent : UiEvent {

    data class QueryChanged(val query: String) : InventoryEvent

    data class ItemClicked(val item: Item) : InventoryEvent

    data object NewItemClicked : InventoryEvent
}
