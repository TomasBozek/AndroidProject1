package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * The catalog as the server has it.
 *
 * Internal to the data layer, like [LocalCatalogDataSource]: what the rest of the app depends on is
 * `CatalogRepository`, and it does not know whether an answer came from the network or the table.
 */
interface RemoteCatalogDataSource {

    suspend fun getCategories(): List<Category>

    suspend fun getProducts(categoryId: String): List<Product>

    /** One product by id, for the deep link a browsing session never fetched it through. */
    suspend fun getProduct(productId: String): Product
}
