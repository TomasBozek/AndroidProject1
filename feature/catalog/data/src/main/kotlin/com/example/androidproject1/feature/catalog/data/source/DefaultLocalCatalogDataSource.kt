package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.data.database.CatalogDao
import com.example.androidproject1.feature.catalog.data.database.CatalogFetchKeys
import com.example.androidproject1.feature.catalog.data.database.toDomain
import com.example.androidproject1.feature.catalog.data.database.toEntity
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * The catalog, read from and written to the database.
 *
 * A list reads as `null` until it has been fetched, and as a list — empty or not — from then on.
 * To `cached` those two mean "never fetched" and "fetched, and there is nothing in it", and the
 * rows alone cannot tell them apart, because both are an empty table. The fetch marker written
 * with the rows is what does, so an empty category emits its empty list once instead of refetching
 * for ever behind a spinner that never stops.
 */
class DefaultLocalCatalogDataSource(
    private val catalogDao: CatalogDao,
    private val now: () -> Long = System::currentTimeMillis,
) : LocalCatalogDataSource {

    override fun observeCategories(): Flow<List<Category>?> =
        catalogDao.observeCategories()
            .combine(catalogDao.observeFetch(CatalogFetchKeys.CATEGORIES)) { rows, fetch ->
                if (fetch == null) null else rows.map { it.toDomain() }
            }

    override fun observeProducts(categoryId: String): Flow<List<Product>?> =
        catalogDao.observeProductsIn(categoryId)
            .combine(catalogDao.observeFetch(CatalogFetchKeys.products(categoryId))) { rows, fetch ->
                if (fetch == null) null else rows.map { it.toDomain() }
            }

    override fun observeAllProducts(): Flow<List<Product>> =
        catalogDao.observeAllProducts().map { rows -> rows.map { it.toDomain() } }

    override fun observeProductsMatching(query: String): Flow<List<Product>> =
        catalogDao.observeProductsMatching(query).map { rows -> rows.map { it.toDomain() } }

    override fun observeProduct(productId: String): Flow<Product?> =
        catalogDao.observeProduct(productId).map { it?.toDomain() }

    override suspend fun replaceCategories(categories: List<Category>) =
        catalogDao.replaceCategories(categories.map { it.toEntity() }, fetchedAt = now())

    override suspend fun replaceProducts(categoryId: String, products: List<Product>) =
        catalogDao.replaceProductsIn(categoryId, products.map { it.toEntity() }, fetchedAt = now())

    override suspend fun getProduct(productId: String): Product? =
        catalogDao.product(productId)?.toDomain()
}
