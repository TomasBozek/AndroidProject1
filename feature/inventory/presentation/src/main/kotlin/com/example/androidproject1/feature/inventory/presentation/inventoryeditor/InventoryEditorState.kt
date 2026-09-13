package com.example.androidproject1.feature.inventory.presentation.inventoryeditor

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.service.core.ui.form.FieldState
import com.example.androidproject1.service.core.ui.form.required
import java.time.LocalDate
import kotlin.math.roundToLong

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
    /**
     * The item as it was loaded, or `null` for a new one. What the title and the save button
     * read, and what [isDirty] compares against — so opening an item and leaving asks nothing.
     */
    val original: Item? = null,
    val name: FieldState = FieldState.of(required()),
    val category: ItemCategory = ItemCategory.Other,
    val condition: ItemCondition = ItemCondition.Good,
    val acquiredOn: LocalDate? = null,
    val quantity: Int = 1,
    /**
     * In minor units, the source of truth: the slider reads [priceFraction] off it and writes it
     * back through [priceMinorFor]. Stored as the fraction, a loaded price would not survive the
     * float round trip and an untouched form would read as dirty.
     */
    val priceMinor: Long = DEFAULT_PRICE_MINOR,
    val insured: Boolean = false,
    val tags: Set<ItemTag> = emptySet(),
    val owner: String = OWNERS.first(),
    val imageUrl: String = "",
    val notes: String = "",
) {

    /** Whether [itemId] named an item — what the title says. */
    val editing: Boolean get() = original != null

    /** Where the price slider sits between zero and [PRICE_MAX_MINOR]. */
    val priceFraction: Float get() = fractionFor(priceMinor)

    /** What the form would save right now. The review reads it; the save writes it. */
    fun toItem(): Item = Item(
        id = itemId,
        name = name.value.trim(),
        category = category,
        condition = condition,
        quantity = quantity,
        priceMinor = priceMinor,
        acquiredOn = acquiredOn,
        insured = insured,
        tags = tags,
        owner = owner,
        imageUrl = imageUrl.trim().ifBlank { null },
        notes = notes.trim(),
    )

    /**
     * Leaving without asking would lose something: for a new item, anything typed; for a loaded
     * one, any difference from what was loaded.
     */
    val isDirty: Boolean
        get() = when (original) {
            null -> name.value.isNotBlank() || notes.isNotBlank() || imageUrl.isNotBlank() || tags.isNotEmpty()
            else -> toItem() != original
        }

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
        const val DEFAULT_PRICE_MINOR = 2_000_00L

        /** The people an item can belong to — a closed list, so the select has something to offer. */
        val OWNERS = listOf("Jana Nováková", "Petr Svoboda", "Eva Dvořáková")

        /** Rounded to whole units of the currency, so a slider never writes a price in cents. */
        fun priceMinorFor(fraction: Float): Long = (fraction * PRICE_MAX_MINOR / 100).roundToLong() * 100

        fun fractionFor(priceMinor: Long): Float =
            (priceMinor.toFloat() / PRICE_MAX_MINOR).coerceIn(0f, 1f)

        /** The form as an existing item fills it — the loaded fields, and the item kept as [original]. */
        fun forItem(item: Item, step: Int = STEP_BASICS) = InventoryEditorState(
            itemId = item.id,
            step = step,
            original = item,
            name = FieldState.of(required(), value = item.name),
            category = item.category,
            condition = item.condition,
            acquiredOn = item.acquiredOn,
            quantity = item.quantity,
            priceMinor = item.priceMinor,
            insured = item.insured,
            tags = item.tags,
            owner = item.owner,
            imageUrl = item.imageUrl.orEmpty(),
            notes = item.notes,
        )

        val PREVIEW = forItem(
            Item(
                id = "item-drill",
                name = "Cordless drill",
                category = ItemCategory.Tools,
                condition = ItemCondition.Good,
                quantity = 1,
                priceMinor = 2_490_00,
                acquiredOn = LocalDate.of(2024, 3, 16),
                insured = false,
                tags = setOf(ItemTag.Lent),
                owner = "Jana Nováková",
                imageUrl = "https://example.com/inventory/drill.jpg",
                notes = "Lent to Petr until the shelves are up.",
            ),
        )
    }
}
