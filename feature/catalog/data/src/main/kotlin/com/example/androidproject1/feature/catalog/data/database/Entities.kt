package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The database's own shape, deliberately not the domain's.
 *
 * `Product` in `domain` is what the app reasons about; these are rows. Keeping them apart is what
 * lets the schema change — a column added for a network field, say — without the change reaching
 * a ViewModel.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val price: Long,
    val description: String,
)

/**
 * A favourite is the product's id and nothing else.
 *
 * No foreign key to `products`: `feat.5` will replace the product table wholesale on every
 * refresh, and a cascade would silently delete the user's favourites with it. A favourite whose
 * product is gone simply does not join, which is the behaviour we want.
 */
@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val productId: String,
    val favouritedAt: Long,
)
