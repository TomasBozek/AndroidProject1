package com.example.androidproject1.feature.catalog.gateway

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * Declared in gateway, implemented in `:feature:catalog:data`. This inversion is what keeps
 * the data layer depending on gateway rather than the other way round.
 */
interface LocalCatalogDataSource {

    suspend fun getCategories(): List<Category>

    suspend fun getProducts(categoryId: String): List<Product>

    suspend fun getProduct(productId: String): Product?
}
