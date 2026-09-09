package com.example.androidproject1.feature.catalog.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.core.domain.error.ServerError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.network.HttpClientFactory
import com.example.androidproject1.core.network.NetworkConfig
import com.example.androidproject1.feature.catalog.data.database.CatalogDatabase
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.DefaultRemoteCatalogDataSource
import com.example.androidproject1.feature.catalog.domain.Category
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROBOLECTRIC_SDK = 35

private const val CATEGORIES_JSON =
    """[{"id":"beverages","name":"Beverages"},{"id":"bakery","name":"Bakery"}]"""

private const val PRODUCTS_JSON =
    """[{"id":"coffee","categoryId":"beverages","name":"Coffee","price":450,"description":"Ground."}]"""

/**
 * Cache-then-network end to end: a real `MockEngine` on one side, a real SQLite on the other.
 *
 * Faking either would leave the interesting part — that what the network returns is written to the
 * table the cache reads from — asserted by nothing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class DefaultCatalogRepositoryTest {

    private lateinit var database: CatalogDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CatalogDatabase::class.java,
        )
            // Room runs queries on its own executor, which the test scheduler knows nothing
            // about — so `advanceUntilIdle` returns while a write is still in flight. Running
            // them inline makes the database part of the test's own timeline.
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    private fun repository(engine: MockEngine) = DefaultCatalogRepository(
        logger = FakeLogger(),
        localCatalogDataSource = DefaultLocalCatalogDataSource(database.catalogDao()),
        remoteCatalogDataSource = DefaultRemoteCatalogDataSource(
            client = HttpClientFactory.create(
                engine = engine,
                config = NetworkConfig(baseUrl = "https://fixtures.test/"),
            ),
            dispatcherProvider = TestDispatchers,
        ),
    )

    private fun serving(json: String) = MockEngine {
        respond(
            content = json,
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
        )
    }

    private fun failing() = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }

    @Test
    fun `a cache miss fetches and returns what the server sent`() = runTest {
        val outcome = repository(serving(CATEGORIES_JSON)).observeCategories().first()

        assertEquals(
            listOf(Category("beverages", "Beverages"), Category("bakery", "Bakery")),
            (outcome as Outcome.Success).data,
        )
    }

    @Test
    fun `what the server sent is written to the cache`() = runTest {
        repository(serving(CATEGORIES_JSON)).observeCategories().first()

        // The point of cache-then-network: the next reader needs no network at all.
        assertEquals(2, database.catalogDao().observeCategories().first().size)
    }

    @Test
    fun `a cache hit is emitted before the network answers`() = runTest {
        repository(serving(CATEGORIES_JSON)).observeCategories().first()

        val emissions = repository(serving(CATEGORIES_JSON)).observeCategories().take(1).toList()

        // The first emission comes from the table, so a warm start draws immediately.
        assertEquals(2, (emissions[0] as Outcome.Success).data.size)
    }

    @Test
    fun `a failure over a stale cache emits the data and then the failure`() = runTest {
        repository(serving(CATEGORIES_JSON)).observeCategories().first()

        val emissions = repository(failing()).observeCategories().toList()

        // Both, in that order: the screen keeps the stale list and shows the error beside it.
        assertEquals(2, emissions.size)
        assertEquals(2, (emissions[0] as Outcome.Success).data.size)
        assertTrue((emissions[1] as Outcome.Failure).error is ServerError)
    }

    @Test
    fun `a failure with nothing cached emits only the failure`() = runTest {
        val emissions = repository(failing()).observeCategories().take(1).toList()

        assertTrue((emissions[0] as Outcome.Failure).error is ServerError)
    }

    @Test
    fun `products are fetched per category and cached`() = runTest {
        val outcome = repository(serving(PRODUCTS_JSON)).observeProducts("beverages").first()

        assertEquals(listOf("Coffee"), (outcome as Outcome.Success).data.map { it.name })
        assertEquals(1, database.catalogDao().observeProductsIn("beverages").first().size)
    }

    @Test
    fun `a product is read from the cache the lists filled, with no network`() = runTest {
        repository(serving(PRODUCTS_JSON)).observeProducts("beverages").first()

        // failing(): opening a product must not be a round trip.
        val outcome = repository(failing()).getProduct("coffee")

        assertEquals("Coffee", (outcome as Outcome.Success).data?.name)
    }

    @Test
    fun `a refresh removes a product the server no longer has`() = runTest {
        repository(serving(PRODUCTS_JSON)).observeProducts("beverages").first()

        // Collected in the background rather than with toList(): once the refresh empties the
        // table the cache maps to null and emits nothing more, so the flow stays live and silent.
        backgroundScope.launch { repository(serving("[]")).observeProducts("beverages").collect {} }

        // Waiting on the table, not on the scheduler: the HTTP call finishes on a dispatcher
        // `advanceUntilIdle` knows nothing about, so idleness is not the same as done. Replace,
        // not upsert — a product the server dropped has to actually disappear.
        assertEquals(
            emptyList<String>(),
            database.catalogDao().observeProductsIn("beverages").first { it.isEmpty() }.map { it.id },
        )
    }
}
