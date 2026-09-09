package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalFavouritesDataSource
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The DAO round trip, against a real SQLite rather than a fake.
 *
 * A fake DAO would assert that this test's own map lookup works; what is worth testing is the SQL
 * — the join that orders favourites, and the `EXISTS` that drives the heart.
 */
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class CatalogDatabaseTest {

    private lateinit var database: CatalogDatabase
    private lateinit var catalog: DefaultLocalCatalogDataSource
    private lateinit var favourites: DefaultLocalFavouritesDataSource

    private var clock = 1_000L

    private val coffee = Product("coffee", "beverages", "Coffee", 450, "Ground.")
    private val tea = Product("tea", "beverages", "Tea", 300, "Loose leaf.")

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
        catalog = DefaultLocalCatalogDataSource(database.catalogDao())
        // A fixed clock, so the ordering assertion below is about the SQL and not about how fast
        // the test machine gets through two inserts.
        favourites = DefaultLocalFavouritesDataSource(database.favouritesDao()) { clock }
    }

    @After
    fun tearDown() = database.close()

    private suspend fun givenProducts(vararg products: Product) =
        catalog.replaceProducts("beverages", products.toList())

    @Test
    fun `an empty table reads as null, not as an empty list`() = runTest {
        // The distinction cache-then-network rests on: null is "never fetched", an empty list is
        // "fetched, and the category really is empty".
        assertNull(catalog.observeProducts("beverages").first())

        catalog.replaceProducts("beverages", emptyList())

        assertNull(catalog.observeProducts("beverages").first())
    }

    @Test
    fun `products written by a refresh are readable`() = runTest {
        givenProducts(coffee, tea)

        assertEquals(listOf("Coffee", "Tea"), catalog.observeProducts("beverages").first()?.map { it.name })
        assertEquals("Coffee", catalog.getProduct("coffee")?.name)
        assertNull(catalog.getProduct("no-such-product"))
    }

    @Test
    fun `a refresh replaces rather than merges`() = runTest {
        givenProducts(coffee, tea)

        givenProducts(coffee)

        // A product the server dropped has to disappear; an upsert would keep it for ever.
        assertEquals(listOf("Coffee"), catalog.observeProducts("beverages").first()?.map { it.name })
    }

    @Test
    fun `favouriting a product round trips`() = runTest {
        givenProducts(coffee)

        assertFalse(favourites.observeIsFavourite("coffee").first())

        favourites.setFavourite("coffee", favourite = true)

        assertTrue(favourites.observeIsFavourite("coffee").first())
        assertEquals(listOf("Coffee"), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `unfavouriting removes it again`() = runTest {
        givenProducts(coffee)
        favourites.setFavourite("coffee", favourite = true)

        favourites.setFavourite("coffee", favourite = false)

        assertFalse(favourites.observeIsFavourite("coffee").first())
        assertEquals(emptyList<String>(), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `favourites come back most recently added first`() = runTest {
        givenProducts(coffee, tea)

        clock = 1_000L
        favourites.setFavourite("coffee", favourite = true)
        clock = 2_000L
        favourites.setFavourite("tea", favourite = true)

        assertEquals(listOf("Tea", "Coffee"), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `re-favouriting moves a product back to the front`() = runTest {
        givenProducts(coffee, tea)
        clock = 1_000L
        favourites.setFavourite("coffee", favourite = true)
        clock = 2_000L
        favourites.setFavourite("tea", favourite = true)

        clock = 3_000L
        favourites.setFavourite("coffee", favourite = true)

        assertEquals(listOf("Coffee", "Tea"), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `two products favourited in the same millisecond keep a stable order`() = runTest {
        givenProducts(coffee, tea)
        // The clock the app ships is System.currentTimeMillis, so two taps inside one millisecond
        // tie — and a tie is ordered by whatever SQLite feels like, which the next REPLACE upsert
        // is free to change. Without a second sort key the list reshuffles for no visible reason.
        clock = 1_000L
        favourites.setFavourite("coffee", favourite = true)
        favourites.setFavourite("tea", favourite = true)
        val before = favourites.observeFavourites().first().map { it.name }

        favourites.setFavourite("coffee", favourite = true)

        assertEquals(before, favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `a favourite whose product a refresh removed simply does not appear`() = runTest {
        givenProducts(coffee)
        favourites.setFavourite("coffee", favourite = true)

        // Exactly what feat.5's refresh does when the server drops a product. There is no cascade,
        // so the favourite survives — and the join stops returning it.
        givenProducts()

        assertTrue(favourites.observeIsFavourite("coffee").first())
        assertEquals(emptyList<String>(), favourites.observeFavourites().first().map { it.name })
    }
}
