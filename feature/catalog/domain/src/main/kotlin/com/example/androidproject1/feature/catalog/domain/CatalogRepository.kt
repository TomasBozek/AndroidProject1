package com.example.androidproject1.feature.catalog.domain

import com.example.androidproject1.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 *
 * Flows rather than one-shot `suspend` calls, because the catalog is cache-then-network: a
 * collector sees what was cached, then what the server said, and then any later change. A
 * `suspend fun` could only return one of those, which is why offline-first cannot be bolted on
 * afterwards without changing this interface.
 */
interface CatalogRepository {

    fun observeCategories(): Flow<Outcome<List<Category>>>

    fun observeProducts(categoryId: String): Flow<Outcome<List<Product>>>

    /** A single product, read from the cache the lists filled. */
    suspend fun getProduct(productId: String): Outcome<Product?>
}
