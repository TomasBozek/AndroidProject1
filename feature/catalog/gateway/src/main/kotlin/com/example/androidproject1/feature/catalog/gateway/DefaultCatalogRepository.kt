package com.example.androidproject1.feature.catalog.gateway

import com.example.androidproject1.core.data.BaseRepository
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

class DefaultCatalogRepository(
    logger: Logger,
    private val localCatalogDataSource: LocalCatalogDataSource,
) : CatalogRepository, BaseRepository(logger = logger.withTag("DefaultCatalogRepository")) {

    override suspend fun getCategories(): Outcome<List<Category>> = execute {
        localCatalogDataSource.getCategories()
    }

    override suspend fun getProducts(categoryId: String): Outcome<List<Product>> = execute {
        localCatalogDataSource.getProducts(categoryId)
    }

    override suspend fun getProduct(productId: String): Outcome<Product?> = execute {
        localCatalogDataSource.getProduct(productId)
    }
}
