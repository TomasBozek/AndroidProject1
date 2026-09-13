package com.example.androidproject1.feature.inventory.presentation.inventory

import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface InventoryEvent : UiEvent {

    data class QueryChanged(val query: String) : InventoryEvent

    data class ItemClicked(val item: Item) : InventoryEvent

    data object NewItemClicked : InventoryEvent

    // The filter sheet.
    data object FilterClicked : InventoryEvent

    data object FilterDismissed : InventoryEvent

    /** `null` is every category. */
    data class FilterCategorySelected(val category: ItemCategory?) : InventoryEvent

    data class FilterTagChanged(val tag: ItemTag, val checked: Boolean) : InventoryEvent

    data class FilterAllTagsChanged(val checked: Boolean) : InventoryEvent

    /** The slider's position; all the way up means no cap. */
    data class FilterMaxPriceChanged(val fraction: Float) : InventoryEvent

    data object FilterCleared : InventoryEvent

    data class SortSelected(val sort: ItemSort) : InventoryEvent

    // Selection mode.
    data class ItemLongPressed(val item: Item) : InventoryEvent

    data class ItemChecked(val item: Item, val checked: Boolean) : InventoryEvent

    data class SelectAllChanged(val checked: Boolean) : InventoryEvent

    data object SelectionCleared : InventoryEvent

    data object DeleteSelectedClicked : InventoryEvent

    /** Marks every selected item a favourite, or clears the mark when all of them already are. */
    data object FavouriteSelectedClicked : InventoryEvent
}
