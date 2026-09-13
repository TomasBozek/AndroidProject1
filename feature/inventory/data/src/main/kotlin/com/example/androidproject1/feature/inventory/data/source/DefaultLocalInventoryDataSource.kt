package com.example.androidproject1.feature.inventory.data.source

import com.example.androidproject1.feature.inventory.data.database.ItemDao
import com.example.androidproject1.feature.inventory.data.database.toDomain
import com.example.androidproject1.feature.inventory.data.database.toEntity
import com.example.androidproject1.feature.inventory.domain.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Seeds [ITEM_FIXTURES] into the table the first time anything asks, exactly as
 * `DefaultLocalDestinationsDataSource` does: before subscribing to the query, because Room's
 * Flow only replays what the table held at subscription time, and a seed from inside a `map`
 * would let the first collection through empty. `INSERT OR IGNORE`, so an item the user has
 * edited or deleted is never put back — the fixtures are a starting point, not a refresh.
 */
class DefaultLocalInventoryDataSource(private val itemDao: ItemDao) : LocalInventoryDataSource {

    private var seeded = false

    override fun observeItems(): Flow<List<Item>> = flow {
        ensureSeeded()
        emitAll(itemDao.observeItems().map { rows -> rows.map { it.toDomain() } })
    }

    override fun observeItem(itemId: String): Flow<Item?> = flow {
        ensureSeeded()
        emitAll(itemDao.observeItem(itemId).map { it?.toDomain() })
    }

    override suspend fun getItem(itemId: String): Item? {
        ensureSeeded()
        return itemDao.getItem(itemId)?.toDomain()
    }

    override suspend fun upsertItem(item: Item) = itemDao.upsert(item.toEntity())

    override suspend fun deleteItems(itemIds: Set<String>) = itemDao.delete(itemIds)

    private suspend fun ensureSeeded() {
        if (seeded) return
        if (itemDao.count() == 0) {
            itemDao.insertAll(ITEM_FIXTURES.map { it.toEntity() })
        }
        seeded = true
    }
}
