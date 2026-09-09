package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.core.network.HttpErrorMapper
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.withContext

/**
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context, and the caller is `viewModelScope` — which is `Dispatchers.Main`.
 *
 * Failures are mapped here, so everything above sees a `DomainError` and never an HTTP status.
 */
class DefaultRemoteCatalogDataSource(
    private val client: HttpClient,
    private val dispatcherProvider: DispatcherProvider,
) : RemoteCatalogDataSource {

    override suspend fun getCategories(): List<Category> = request {
        client.get("categories").body<List<CategoryDto>>().map { it.toDomain() }
    }

    override suspend fun getProducts(categoryId: String): List<Product> = request {
        client.get("products") { parameter("categoryId", categoryId) }
            .body<List<ProductDto>>()
            .map { it.toDomain() }
    }

    private suspend fun <T> request(block: suspend () -> T): T = withContext(dispatcherProvider.io) {
        try {
            block()
        } catch (throwable: Throwable) {
            throw HttpErrorMapper.map(throwable)
        }
    }
}
