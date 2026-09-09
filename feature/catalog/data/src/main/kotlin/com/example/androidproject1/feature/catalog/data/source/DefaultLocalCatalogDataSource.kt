package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.data.database.CatalogDao
import com.example.androidproject1.feature.catalog.data.database.toDomain
import com.example.androidproject1.feature.catalog.data.database.toEntity
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * The catalog, read from the database.
 *
 * Seeding happens on first read rather than in a `RoomDatabase.Callback`: the callback runs on
 * whichever thread opened the database and cannot suspend, so it would need its own scope and a
 * second set of insert paths. Checking a count is cheap, the inserts replace on conflict, and the
 * whole thing is reachable from a test without opening the database twice.
 */
class DefaultLocalCatalogDataSource(
    private val catalogDao: CatalogDao,
) : LocalCatalogDataSource {

    override suspend fun getCategories(): List<Category> {
        seedIfEmpty()
        return catalogDao.categories().map { it.toDomain() }
    }

    override suspend fun getProducts(categoryId: String): List<Product> {
        seedIfEmpty()
        return catalogDao.productsIn(categoryId).map { it.toDomain() }
    }

    override suspend fun getProduct(productId: String): Product? {
        seedIfEmpty()
        return catalogDao.product(productId)?.toDomain()
    }

    private suspend fun seedIfEmpty() {
        if (catalogDao.productCount() > 0) return
        catalogDao.insertCategories(CatalogSeed.CATEGORIES.map { it.toEntity() })
        catalogDao.insertProducts(CatalogSeed.PRODUCTS.map { it.toEntity() })
    }
}
