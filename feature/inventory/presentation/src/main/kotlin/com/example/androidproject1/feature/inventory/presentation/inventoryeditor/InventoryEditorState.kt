package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.service.core.ui.form.FieldState
import com.example.androidproject1.service.core.ui.form.required
import java.time.LocalDate

/**
 * Create and edit are one screen: the editor edits the item with [itemId] if one exists and starts
 * blank if none does — a new item is a new id, minted by the list.
 */
@Immutable
data class InventoryEditorState(
    val itemId: String,
    val step: Int = STEP_BASICS,
    /** The existing item is being read; the form draws blank until it lands. */
    val loading: Boolean = false,
    /** Whether [itemId] named an item — what the title says and what the review calls the save. */
    val editing: Boolean = false,
    val name: FieldState = FieldState.of(required()),
    val category: ItemCategory = ItemCategory.Other,
    val condition: ItemCondition = ItemCondition.Good,
    val acquiredOn: LocalDate? = null,
    val quantity: Int = 1,
    /** Where the price slider sits between zero and [PRICE_MAX_MINOR]. */
    val priceFraction: Float = DEFAULT_PRICE_FRACTION,
    val insured: Boolean = false,
    val tags: Set<ItemTag> = emptySet(),
    val owner: String = OWNERS.first(),
    val imageUrl: String = "",
    val notes: String = "",
) {

    val priceMinor: Long get() = priceMinorFor(priceFraction)

    /** Enough has been typed that leaving without asking would lose something. */
    val isDirty: Boolean
        get() = name.value.isNotBlank() || notes.isNotBlank() || imageUrl.isNotBlank() || tags.isNotEmpty()

    /** The master checkbox over the tags: all, none, or some. */
    val allTagsState: CheckState
        get() = when (tags.size) {
            0 -> CheckState.Off
            ItemTag.entries.size -> CheckState.On
            else -> CheckState.Indeterminate
        }

    val canContinue: Boolean
        get() = when (step) {
            STEP_BASICS -> name.isValid
            STEP_QUANTITY -> quantity > 0
            else -> true
        }

    companion object {

        const val STEP_BASICS = 0
        const val STEP_QUANTITY = 1
        const val STEP_TAGS = 2
        const val STEP_REVIEW = 3
        const val STEP_COUNT = 4

        const val PRICE_MAX_MINOR = 20_000_00L
        const val DEFAULT_PRICE_FRACTION = 0.1f

        /** The people an item can belong to — a closed list, so the select has something to offer. */
        val OWNERS = listOf("Jana Nováková", "Petr Svoboda", "Eva Dvořáková")

        fun priceMinorFor(fraction: Float): Long = (fraction * PRICE_MAX_MINOR).toLong()

        fun fractionFor(priceMinor: Long): Float =
            (priceMinor.toFloat() / PRICE_MAX_MINOR).coerceIn(0f, 1f)

        val PREVIEW = InventoryEditorState(
            itemId = "item-drill",
            editing = true,
            name = FieldState.of(required(), value = "Cordless drill"),
            category = ItemCategory.Tools,
            condition = ItemCondition.Good,
            acquiredOn = LocalDate.of(2024, 3, 16),
            quantity = 1,
            priceFraction = fractionFor(2_490_00),
            tags = setOf(ItemTag.Lent),
            owner = "Jana Nováková",
            imageUrl = "https://example.com/inventory/drill.jpg",
            notes = "Lent to Petr until the shelves are up.",
        )
    }
}
