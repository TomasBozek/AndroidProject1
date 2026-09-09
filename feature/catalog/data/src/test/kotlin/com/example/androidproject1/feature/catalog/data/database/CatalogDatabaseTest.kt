package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.catalog.data.source.CatalogSeed
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalCatalogDataSource
import com.example.androidproject1.feature.catalog.data.source.DefaultLocalFavouritesDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * — the join that orders favourites, and the `EXISTS` that drives the heart. Room's in-memory
 * builder runs under Robolectric as an ordinary unit test, so `./gradlew test` covers it.
 */
private const val ROBOLECTRIC_SDK = 35

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class CatalogDatabaseTest {

    private lateinit var database: CatalogDatabase
    private lateinit var catalog: DefaultLocalCatalogDataSource
    private lateinit var favourites: DefaultLocalFavouritesDataSource

    private var clock = 1_000L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CatalogDatabase::class.java,
        ).build()
        catalog = DefaultLocalCatalogDataSource(database.catalogDao())
        // A fixed clock, so the ordering assertion below is about the SQL and not about how fast
        // the test machine gets through two inserts.
        favourites = DefaultLocalFavouritesDataSource(database.favouritesDao()) { clock }
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `the first read seeds the catalog`() = runTest {
        assertEquals(0, database.catalogDao().productCount())

        val products = catalog.getProducts("beverages")

        assertEquals(CatalogSeed.PRODUCTS.count { it.categoryId == "beverages" }, products.size)
        assertEquals(CatalogSeed.PRODUCTS.size, database.catalogDao().productCount())
    }

    @Test
    fun `seeding runs once, not on every read`() = runTest {
        catalog.getCategories()
        catalog.getCategories()

        assertEquals(CatalogSeed.CATEGORIES.size, database.catalogDao().categories().size)
    }

    @Test
    fun `a product is found by id and a missing one is null`() = runTest {
        assertEquals("Coffee", catalog.getProduct("coffee")?.name)
        assertEquals(null, catalog.getProduct("no-such-product"))
    }

    @Test
    fun `favouriting a product round trips`() = runTest {
        catalog.getProducts("beverages")

        assertFalse(favourites.observeIsFavourite("coffee").first())

        favourites.setFavourite("coffee", favourite = true)

        assertTrue(favourites.observeIsFavourite("coffee").first())
        assertEquals(listOf("Coffee"), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `unfavouriting removes it again`() = runTest {
        catalog.getProducts("beverages")
        favourites.setFavourite("coffee", favourite = true)

        favourites.setFavourite("coffee", favourite = false)

        assertFalse(favourites.observeIsFavourite("coffee").first())
        assertEquals(emptyList<String>(), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `favourites come back most recently added first`() = runTest {
        catalog.getProducts("beverages")

        clock = 1_000L
        favourites.setFavourite("coffee", favourite = true)
        clock = 2_000L
        favourites.setFavourite("tea", favourite = true)

        assertEquals(listOf("Tea", "Coffee"), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `re-favouriting moves a product back to the front`() = runTest {
        catalog.getProducts("beverages")
        clock = 1_000L
        favourites.setFavourite("coffee", favourite = true)
        clock = 2_000L
        favourites.setFavourite("tea", favourite = true)

        clock = 3_000L
        favourites.setFavourite("coffee", favourite = true)

        assertEquals(listOf("Coffee", "Tea"), favourites.observeFavourites().first().map { it.name })
    }

    @Test
    fun `a favourite whose product is gone simply does not appear`() = runTest {
        // What feat.5 will do on every refresh: replace the product table. The favourite row
        // survives — there is no cascade — and the join stops returning it.
        favourites.setFavourite("coffee", favourite = true)

        assertTrue(favourites.observeIsFavourite("coffee").first())
        assertEquals(emptyList<String>(), favourites.observeFavourites().first().map { it.name })
    }
}
