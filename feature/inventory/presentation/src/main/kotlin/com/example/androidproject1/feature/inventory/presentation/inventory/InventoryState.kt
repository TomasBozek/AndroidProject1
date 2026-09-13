package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.compose.runtime.Immutable
import com.example.androidproject1.core.ui.component.CheckState
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag

/** What the filter sheet holds. Every field off means "everything", which is what [isEmpty] says. */
@Immutable
data class ItemFilter(
    val category: ItemCategory? = null,
    val tags: Set<ItemTag> = emptySet(),
    /** `null` is no cap; otherwise the slider's position in minor units. */
    val maxPriceMinor: Long? = null,
) {

    val isEmpty: Boolean get() = category == null && tags.isEmpty() && maxPriceMinor == null

    /** What the badge on the filter button shows: one per criterion in force. */
    val activeCount: Int
        get() = listOf(category != null, tags.isNotEmpty(), maxPriceMinor != null).count { it }

    fun matches(item: Item): Boolean =
        (category == null || item.category == category) &&
            item.tags.containsAll(tags) &&
            (maxPriceMinor == null || item.priceMinor <= maxPriceMinor)
}

enum class ItemSort { Name, Price, Acquired }

@Immutable
data class InventoryState(
    /** What the search field holds; the rows are what is left after it, the filter and the sort. */
    val query: String = "",
    /** The rows to draw — already filtered and sorted, so the screen never searches or sorts. */
    val items: List<Item> = emptyList(),
    /**
     * The first emission is in flight — drawn as skeleton rows rather than the shared overlay,
     * since there is already a real shell (the top bar, the search field, the FAB) to show it under.
     */
    val loading: Boolean = false,
    val filter: ItemFilter = ItemFilter(),
    val filterSheetOpen: Boolean = false,
    val sort: ItemSort = ItemSort.Name,
    /** A long press on a row starts this; empty means the ordinary list. */
    val selectedIds: Set<String> = emptySet(),
    /** Rows whose write is in flight — drawn with a spinner in the trailing slot. */
    val pendingIds: Set<String> = emptySet(),
) {

    val selecting: Boolean get() = selectedIds.isNotEmpty()

    /** The toolbar's master checkbox, over the rows on screen. */
    val selectAllState: CheckState
        get() = when {
            selectedIds.isEmpty() -> CheckState.Off
            items.all { it.id in selectedIds } -> CheckState.On
            else -> CheckState.Indeterminate
        }

    /** The sheet's master checkbox over the tags: all, none, or some. */
    val allFilterTagsState: CheckState
        get() = when (filter.tags.size) {
            0 -> CheckState.Off
            ItemTag.entries.size -> CheckState.On
            else -> CheckState.Indeterminate
        }

    companion object {

        const val PRICE_MAX_MINOR = 20_000_00L

        val PREVIEW = InventoryState(
            items = listOf(
                Item(
                    id = "item-drill",
                    name = "Cordless drill",
                    category = ItemCategory.Tools,
                    condition = ItemCondition.Good,
                    quantity = 1,
                    priceMinor = 2_490_00,
                    acquiredOn = null,
                    insured = false,
                    tags = setOf(ItemTag.Lent),
                    owner = "Jana Nováková",
                    imageUrl = null,
                    notes = "",
                ),
                Item(
                    id = "item-kettle",
                    name = "Electric kettle",
                    category = ItemCategory.Electronics,
                    condition = ItemCondition.New,
                    quantity = 1,
                    priceMinor = 890_00,
                    acquiredOn = null,
                    insured = false,
                    tags = setOf(ItemTag.Favourite),
                    owner = "Petr Svoboda",
                    imageUrl = null,
                    notes = "",
                ),
                Item(
                    id = "item-chair",
                    name = "Office chair",
                    category = ItemCategory.Furniture,
                    condition = ItemCondition.Worn,
                    quantity = 2,
                    priceMinor = 3_200_00,
                    acquiredOn = null,
                    insured = false,
                    tags = setOf(ItemTag.ForSale),
                    owner = "Eva Dvořáková",
                    imageUrl = null,
                    notes = "",
                ),
            ),
        )
    }
}
