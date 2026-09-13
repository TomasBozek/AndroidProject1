package com.example.androidproject1.feature.inventory.presentation.inventorydetail

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import java.time.LocalDate

@Immutable
data class InventoryDetailState(
    val itemId: String,
    /** `null` until the first emission lands — the shell draws, the sections wait. */
    val item: Item? = null,
    val selectedSection: Int = SECTION_OVERVIEW,
) {

    companion object {

        const val SECTION_OVERVIEW = 0
        const val SECTION_NOTES = 1
        const val SECTION_HISTORY = 2
        const val SECTION_COUNT = 3

        val PREVIEW = InventoryDetailState(
            itemId = "item-drill",
            item = Item(
                id = "item-drill",
                name = "Cordless drill",
                category = ItemCategory.Tools,
                condition = ItemCondition.Good,
                quantity = 1,
                priceMinor = 2_490_00,
                acquiredOn = LocalDate.of(2024, 3, 16),
                insured = true,
                tags = setOf(ItemTag.Lent, ItemTag.Favourite),
                owner = "Jana Nováková",
                imageUrl = "https://example.com/inventory/drill.jpg",
                notes = "Lent to Petr until the shelves are up.",
            ),
        )
    }
}
