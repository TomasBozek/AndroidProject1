package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface InventoryDetailEvent : UiEvent {

    data class SectionSelected(val index: Int) : InventoryDetailEvent

    data object EditClicked : InventoryDetailEvent

    data object DeleteClicked : InventoryDetailEvent

    data object NavigateUpClicked : InventoryDetailEvent
}
