package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.Flow

/**
 * The cache. Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:catalog:data` names it. What the rest of the app depends on is `CatalogRepository`.
 *
 * The reads return `null` for "nothing cached" rather than an empty list, because
 * `BaseRepository.cached` has to tell a cache miss from a cached empty result — a category the
 * server really has no products in should not re-fetch on every collection.
 */
interface LocalCatalogDataSource {

    fun observeCategories(): Flow<List<Category>?>

    fun observeProducts(categoryId: String): Flow<List<Product>?>

    /** Everything cached, for the picker. Never null: an empty catalog is an empty list here. */
    fun observeAllProducts(): Flow<List<Product>>

    /** Cached products matching a search. Never null: no match is an empty list, not a miss. */
    fun observeProductsMatching(query: String): Flow<List<Product>>

    fun observeProduct(productId: String): Flow<Product?>

    suspend fun replaceCategories(categories: List<Category>)

    suspend fun replaceProducts(categoryId: String, products: List<Product>)

    suspend fun getProduct(productId: String): Product?
}
