package com.example.androidproject1.feature.inventory.data.repository

import com.example.androidproject1.feature.inventory.data.source.LocalInventoryDataSource
import com.example.androidproject1.feature.inventory.domain.InventoryRepository
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultInventoryRepository(
    logger: Logger,
    private val localInventoryDataSource: LocalInventoryDataSource,
) : InventoryRepository, BaseRepository(logger = logger.withTag("DefaultInventoryRepository")) {

    // retries, because the list and the detail hold this collector for as long as the screen
    // exists — one transient database error must not stop it emitting for good.
    override fun observeItems(): Flow<Outcome<List<Item>>> =
        observe(source = localInventoryDataSource.observeItems(), retries = RETRIES)

    override fun observeItem(itemId: String): Flow<Outcome<Item?>> =
        observe(source = localInventoryDataSource.observeItem(itemId), retries = RETRIES)

    override suspend fun getItem(itemId: String): Outcome<Item?> = execute { localInventoryDataSource.getItem(itemId) }

    override suspend fun saveItem(item: Item): Outcome<Unit> = execute { localInventoryDataSource.upsertItem(item) }

    override suspend fun deleteItems(itemIds: Set<String>): Outcome<Unit> =
        execute { localInventoryDataSource.deleteItems(itemIds) }

    private companion object {

        const val RETRIES = 3L
    }
}
