package com.example.androidproject1.feature.inventory.domain

import java.time.LocalDate

/** Where an item belongs. The closed set a picker offers, so a filter can be exhaustive. */
enum class ItemCategory { Tools, Electronics, Furniture, Books, Other }

/** What state an item is in — what a tag on the row says, in three words. */
enum class ItemCondition { New, Good, Worn }

/** Facts about an item that are on or off, and any number of them at once. */
enum class ItemTag { Fragile, Lent, ForSale, Favourite }

/**
 * Something the user owns. A field for every kind of control the design system has, which is the
 * point of the feature (E3S1): a category for a select, a condition for a segmented control, a
 * quantity for a stepper, a price for a slider, an acquisition date for a date field, an insured
 * flag for a switch, tags for checkboxes, an owner for an avatar, a picture for an image and
 * notes for a text area.
 *
 * @property priceMinor in minor units, like `Product.price` — a `Long` that is never a float.
 * @property owner a name, which is what `AppAvatar` draws its initials from.
 * @property imageUrl a fixture URL the offline `dev` build never loads. That is the honest
 * `AppImage` state rather than a defect: D20 stands, and nothing here fetches.
 */
data class Item(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val condition: ItemCondition,
    val quantity: Int,
    val priceMinor: Long,
    val acquiredOn: LocalDate?,
    val insured: Boolean,
    val tags: Set<ItemTag>,
    val owner: String,
    val imageUrl: String?,
    val notes: String,
)
