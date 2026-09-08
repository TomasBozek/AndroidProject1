package com.example.androidproject1.feature.catalog.domain

import com.example.androidproject1.core.domain.result.Outcome

/**
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 */
interface CatalogRepository {

    suspend fun getCategories(): Outcome<List<Category>>

    suspend fun getProducts(categoryId: String): Outcome<List<Product>>

    suspend fun getProduct(productId: String): Outcome<Product?>
}
