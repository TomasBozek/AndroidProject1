package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.service.core.ui.event.UiEvent
import java.time.LocalDate

sealed interface InventoryEditorEvent : UiEvent {

    data class NameChanged(val name: String) : InventoryEditorEvent

    data class CategorySelected(val category: ItemCategory) : InventoryEditorEvent

    data class ConditionSelected(val condition: ItemCondition) : InventoryEditorEvent

    data class AcquiredOnChanged(val date: LocalDate) : InventoryEditorEvent

    data class QuantityChanged(val quantity: Int) : InventoryEditorEvent

    data class PriceChanged(val fraction: Float) : InventoryEditorEvent

    data class InsuredChanged(val insured: Boolean) : InventoryEditorEvent

    data class TagChanged(val tag: ItemTag, val checked: Boolean) : InventoryEditorEvent

    /** The master checkbox: on ticks every tag, off clears them. */
    data class AllTagsChanged(val checked: Boolean) : InventoryEditorEvent

    data class OwnerSelected(val owner: String) : InventoryEditorEvent

    data class ImageUrlChanged(val imageUrl: String) : InventoryEditorEvent

    data class NotesChanged(val notes: String) : InventoryEditorEvent

    data object NextClicked : InventoryEditorEvent

    data object NavigateUpClicked : InventoryEditorEvent

    /** The system back gesture on a dirty form; the screen asked, the ViewModel decides. */
    data object BackRequested : InventoryEditorEvent
}
