package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * Internal to the data layer: it sits beside its implementation in `source`, and nothing above
 * `:feature:catalog:data` names it. What the rest of the app depends on is `CatalogRepository`,
 * which lives in `domain`.
 */
interface LocalCatalogDataSource {

    suspend fun getCategories(): List<Category>

    suspend fun getProducts(categoryId: String): List<Product>

    suspend fun getProduct(productId: String): Product?
}
