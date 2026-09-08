package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.error.NetworkError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/** In-memory [CatalogRepository]. Set [failWith] to make every call fail. */
class FakeCatalogRepository(
    var categories: List<Category> = listOf(Category(id = "beverages", name = "Beverages")),
    var products: List<Product> = listOf(COFFEE),
    var failWith: DomainError? = null,
) : CatalogRepository {

    var getProductsCallCount = 0
        private set

    override suspend fun getCategories(): Outcome<List<Category>> =
        failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(categories)

    override suspend fun getProducts(categoryId: String): Outcome<List<Product>> {
        getProductsCallCount++
        return failWith?.let { Outcome.Failure(it) }
            ?: Outcome.Success(products.filter { it.categoryId == categoryId })
    }

    override suspend fun getProduct(productId: String): Outcome<Product?> =
        failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(products.find { it.id == productId })

    companion object {

        val COFFEE = Product(
            id = "coffee",
            categoryId = "beverages",
            name = "Coffee",
            price = 450,
            description = "Freshly ground, brewed to order.",
        )

        fun offline() = FakeCatalogRepository(failWith = NetworkError())
    }
}
