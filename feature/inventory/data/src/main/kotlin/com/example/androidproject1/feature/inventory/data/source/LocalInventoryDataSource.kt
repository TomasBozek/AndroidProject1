package com.example.androidproject1.feature.inventory.data.source

import com.example.androidproject1.feature.inventory.domain.Item
import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer: what the rest of the app depends on is `InventoryRepository` in
 * `domain`.
 */
interface LocalInventoryDataSource {

    fun observeItems(): Flow<List<Item>>

    fun observeItem(itemId: String): Flow<Item?>

    suspend fun getItem(itemId: String): Item?

    suspend fun upsertItem(item: Item)

    suspend fun deleteItems(itemIds: Set<String>)
}
