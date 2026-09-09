package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.data.database.CatalogDao
import com.example.androidproject1.feature.catalog.data.database.toDomain
import com.example.androidproject1.feature.catalog.data.database.toEntity
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The catalog, read from and written to the database.
 *
 * An empty table reads as `null`, not as an empty list: to `cached` those mean "never fetched" and
 * "fetched, and there is nothing", and collapsing them makes an genuinely empty category re-fetch
 * for ever. Once the remote has written, even an empty write is a list.
 */
class DefaultLocalCatalogDataSource(
    private val catalogDao: CatalogDao,
) : LocalCatalogDataSource {

    override fun observeCategories(): Flow<List<Category>?> =
        catalogDao.observeCategories().map { rows ->
            rows.takeIf { it.isNotEmpty() }?.map { it.toDomain() }
        }

    override fun observeProducts(categoryId: String): Flow<List<Product>?> =
        catalogDao.observeProductsIn(categoryId).map { rows ->
            rows.takeIf { it.isNotEmpty() }?.map { it.toDomain() }
        }

    override fun observeProduct(productId: String): Flow<Product?> =
        catalogDao.observeProduct(productId).map { it?.toDomain() }

    override suspend fun replaceCategories(categories: List<Category>) =
        catalogDao.replaceCategories(categories.map { it.toEntity() })

    override suspend fun replaceProducts(categoryId: String, products: List<Product>) =
        catalogDao.replaceProductsIn(categoryId, products.map { it.toEntity() })

    override suspend fun getProduct(productId: String): Product? =
        catalogDao.product(productId)?.toDomain()
}
