package com.example.androidproject1.feature.inventory.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import java.time.LocalDate

/**
 * The database's own shape, deliberately not the domain's — see `CategoryEntity` in
 * `:feature:catalog` for why. The three enum-typed columns, the date and the tag set are stored as `TEXT`
 * through [Converters], the way E1H1 settled it, so an unknown name reads as a fallback rather than
 * failing the whole list.
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
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
