package com.example.androidproject1.feature.catalog.domain.test

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.error.NetworkError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * In-memory [CatalogRepository], beside the interface it fakes so `:feature:catalog` and anything
 * else that shows a product can share one copy.
 *
 * Set [failWith] to make every call fail. [staleThenFail] is the shape `cached` produces when the
 * network is down over a populated cache — data first, failure second — which is the case a screen
 * showing an inline error over a working list has to handle.
 */
class FakeCatalogRepository(
    var categories: List<Category> = listOf(Category(id = "beverages", name = "Beverages")),
    var products: List<Product> = listOf(COFFEE),
    var failWith: DomainError? = null,
    var staleThenFail: Boolean = false,
) : CatalogRepository {

    var observeProductsCallCount = 0
        private set

    override fun observeCategories(): Flow<Outcome<List<Category>>> = emissions(categories)

    override fun observeProducts(categoryId: String): Flow<Outcome<List<Product>>> {
        observeProductsCallCount++
        return emissions(products.filter { it.categoryId == categoryId })
    }

    override fun observeAllProducts(): Flow<Outcome<List<Product>>> = emissions(products)

    override suspend fun getProduct(productId: String): Outcome<Product?> =
        failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(products.find { it.id == productId })

    private fun <T> emissions(data: T): Flow<Outcome<T>> {
        val error = failWith
        return when {
            error != null && staleThenFail -> flowOf(Outcome.Success(data), Outcome.Failure(error))
            error != null -> flowOf(Outcome.Failure(error))
            else -> flowOf(Outcome.Success(data))
        }
    }

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
