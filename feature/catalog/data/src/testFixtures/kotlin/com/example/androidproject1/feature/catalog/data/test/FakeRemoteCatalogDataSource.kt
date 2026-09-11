package com.example.androidproject1.feature.catalog.data.test

import com.example.androidproject1.feature.catalog.data.source.RemoteCatalogDataSource
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * In-memory [RemoteCatalogDataSource] for tests: a server outage is a settable field, not a
 * `MockEngine`.
 *
 * `DefaultCatalogRepositoryTest` deliberately keeps its own `MockEngine` — cache-then-network is
 * the thing it asserts, and faking either side would leave that untested. This fixture is for a
 * narrower test that wants the network's failure without standing up an HTTP client to produce it.
 *
 * @property categories returned by [getCategories].
 * @property products returned by [getProducts], keyed by category id.
 * @property failWith thrown from both instead of answering.
 */
class FakeRemoteCatalogDataSource(
    var categories: List<Category> = emptyList(),
    var products: Map<String, List<Product>> = emptyMap(),
    var failWith: Throwable? = null,
) : RemoteCatalogDataSource {

    override suspend fun getCategories(): List<Category> {
        failWith?.let { throw it }
        return categories
    }

    override suspend fun getProducts(categoryId: String): List<Product> {
        failWith?.let { throw it }
        return products[categoryId].orEmpty()
    }
}
