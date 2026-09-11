package com.example.androidproject1.feature.catalog.data.repository

import com.example.androidproject1.feature.catalog.data.source.LocalCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.RemoteCatalogDataSource
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Cache, then network, then the cache again — `core.2`'s combinator, which is why this class is
 * four lines of wiring rather than a state machine.
 *
 * The remote writes into the same database `feat.1` gave favourites, so a favourited product that
 * the refresh removes stops appearing on Home without anything here knowing favourites exist.
 */
class DefaultCatalogRepository(
    logger: Logger,
    private val localCatalogDataSource: LocalCatalogDataSource,
    private val remoteCatalogDataSource: RemoteCatalogDataSource,
) : CatalogRepository, BaseRepository(logger = logger.withTag("DefaultCatalogRepository")) {

    override fun observeCategories(): Flow<Outcome<List<Category>>> = cached(
        local = localCatalogDataSource.observeCategories(),
        remote = { remoteCatalogDataSource.getCategories() },
        write = { localCatalogDataSource.replaceCategories(it) },
    )

    override fun observeProducts(categoryId: String): Flow<Outcome<List<Product>>> = cached(
        local = localCatalogDataSource.observeProducts(categoryId),
        remote = { remoteCatalogDataSource.getProducts(categoryId) },
        write = { localCatalogDataSource.replaceProducts(categoryId, it) },
    )

    // Not cached() either: the picker is reached from the cart, and the catalog has already been
    // fetched by the tab the user browsed to get there.
    override fun observeAllProducts(): Flow<Outcome<List<Product>>> =
        observe(source = localCatalogDataSource.observeAllProducts(), retries = RETRIES)

    // Not cached() either, and for the same reason: a search reads what browsing has already
    // fetched. A round trip per keystroke is what the debounce upstream exists to avoid.
    override fun searchProducts(query: String): Flow<Outcome<List<Product>>> =
        observe(source = localCatalogDataSource.observeProductsMatching(query), retries = RETRIES)

    // Not cached(): the happy path is a list already having fetched this row, so the read stays
    // plain. A deep link is the exception — nobody has fetched anything yet — so a miss falls
    // back to the network once and caches what it gets, rather than reporting a real product as
    // one that does not exist.
    override suspend fun getProduct(productId: String): Outcome<Product?> = execute {
        localCatalogDataSource.getProduct(productId)
            ?: remoteCatalogDataSource.getProduct(productId).also { localCatalogDataSource.storeProduct(it) }
    }

    private companion object {

        const val RETRIES = 3L
    }
}
