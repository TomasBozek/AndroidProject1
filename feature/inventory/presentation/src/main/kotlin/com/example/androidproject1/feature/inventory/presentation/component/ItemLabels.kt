package com.example.androidproject1.feature.inventory.presentation.component

import androidx.annotation.StringRes
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import com.example.androidproject1.feature.inventory.presentation.R

/** The word for a category, decided once for the editor, the detail and the filter sheet. */
@StringRes
fun ItemCategory.labelRes(): Int = when (this) {
    ItemCategory.Tools -> R.string.inventory_category_tools
    ItemCategory.Electronics -> R.string.inventory_category_electronics
    ItemCategory.Furniture -> R.string.inventory_category_furniture
    ItemCategory.Books -> R.string.inventory_category_books
    ItemCategory.Other -> R.string.inventory_category_other
}

/** The word for a tag, decided once for the editor, the detail and the filter sheet. */
@StringRes
fun ItemTag.labelRes(): Int = when (this) {
    ItemTag.Fragile -> R.string.inventory_tag_fragile
    ItemTag.Lent -> R.string.inventory_tag_lent
    ItemTag.ForSale -> R.string.inventory_tag_for_sale
    ItemTag.Favourite -> R.string.inventory_tag_favourite
}

/** The word for a condition — the tag on a row, the radio in the editor and the review all agree. */
@StringRes
fun ItemCondition.labelRes(): Int = when (this) {
    ItemCondition.New -> R.string.inventory_condition_new
    ItemCondition.Good -> R.string.inventory_condition_good
    ItemCondition.Worn -> R.string.inventory_condition_worn
}
