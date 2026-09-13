package com.example.androidproject1.feature.inventory.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/** Implemented in the data layer. Declared here so the domain layer depends on nothing. */
interface InventoryRepository {

    fun observeItems(): Flow<Outcome<List<Item>>>

    /** Emits again on every write to this item, and `null` once it is deleted. */
    fun observeItem(itemId: String): Flow<Outcome<Item?>>

    suspend fun getItem(itemId: String): Outcome<Item?>

    suspend fun saveItem(item: Item): Outcome<Unit>

    suspend fun deleteItems(itemIds: Set<String>): Outcome<Unit>
}
