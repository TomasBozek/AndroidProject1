package com.example.androidproject1.feature.inventory.presentation.inventory

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag

@Immutable
data class InventoryState(
    /** What the search field holds; the rows are [items] filtered by it, in the view model. */
    val query: String = "",
    /** The rows to draw — already filtered, so the screen never searches. */
    val items: List<Item> = emptyList(),
    /**
     * The first emission is in flight — drawn as skeleton rows rather than the shared overlay,
     * since there is already a real shell (the top bar, the search field, the FAB) to show it under.
     */
    val loading: Boolean = false,
) {

    companion object {

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
