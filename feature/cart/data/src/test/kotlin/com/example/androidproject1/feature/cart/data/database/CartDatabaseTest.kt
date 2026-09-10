package com.example.androidproject1.feature.cart.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.cart.data.source.DefaultLocalCartDataSource
import com.example.androidproject1.feature.cart.domain.CartItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Robolectric ships an SDK image per API level and has none for this project's targetSdk. */

/**
 * The cart's DAO round trip, against a real SQLite rather than a fake.
 *
 * The rules a cart has are all rules about rows — a second tap adds to a quantity instead of
 * making a second line, a quantity of zero is a removal, the badge is a sum and not a count — so a
 * fake DAO would test this file's own bookkeeping and none of the app's.
 */
@RunWith(RobolectricTestRunner::class)
class CartDatabaseTest {

    private lateinit var database: CartDatabase
    private lateinit var cart: DefaultLocalCartDataSource

    /** Fixed, so an ordering assertion is about the SQL and not about how fast two inserts run. */
    private var clock = 1_000L

    private val coffee = CartItem("coffee", "Coffee", price = 450, quantity = 1)
    private val tea = CartItem("tea", "Tea", price = 300, quantity = 1)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CartDatabase::class.java,
        )
            // Room runs queries on its own executor, which the test scheduler knows nothing
            // about — so `advanceUntilIdle` returns while a write is still in flight. Running
            // them inline makes the database part of the test's own timeline.
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .allowMainThreadQueries()
            .build()
        cart = DefaultLocalCartDataSource(database.cartDao()) { clock }
    }

    @After
    fun tearDown() = database.close()

    private suspend fun items() = cart.observeItems().first()

    private suspend fun names() = items().map { it.name }

    @Test
    fun `an empty cart holds nothing and counts nothing`() = runTest {
        assertEquals(emptyList<CartItem>(), items())
        // COALESCE, not a null: the badge reads this on a cold start with an empty table.
        assertEquals(0, cart.observeCount().first())
    }

    @Test
    fun `an added product keeps the price it was added at`() = runTest {
        cart.add(coffee.copy(price = 450))

        assertEquals(listOf(coffee.copy(price = 450)), items())
    }

    @Test
    fun `adding the same product again adds to its quantity`() = runTest {
        cart.add(coffee)

        cart.add(coffee.copy(quantity = 2))

        // One line, not two: a cart with "Coffee" twice is a bug the user has to fix by hand.
        assertEquals(listOf(3), items().map { it.quantity })
    }

    @Test
    fun `a second tap leaves the row where it was`() = runTest {
        clock = 1_000L
        cart.add(coffee)
        clock = 2_000L
        cart.add(tea)

        clock = 3_000L
        cart.add(coffee)

        // Topping something up must not send it to the bottom of the list under the user's finger.
        assertEquals(listOf("Coffee", "Tea"), names())
    }

    @Test
    fun `a second tap leaves the row where it was even when two were added at once`() = runTest {
        // The same rule, with the clock the app actually ships: two products added inside one
        // millisecond tie, and `now()` is System.currentTimeMillis.
        cart.add(coffee)
        cart.add(tea)

        cart.add(coffee)

        assertEquals(listOf("Coffee", "Tea"), names())
    }

    @Test
    fun `the count is the sum of the quantities, not the number of lines`() = runTest {
        cart.add(coffee.copy(quantity = 2))
        cart.add(tea.copy(quantity = 3))

        assertEquals(5, cart.observeCount().first())
    }

    @Test
    fun `setting a quantity replaces it rather than adding to it`() = runTest {
        cart.add(coffee.copy(quantity = 2))

        cart.setQuantity("coffee", quantity = 5)

        assertEquals(listOf(5), items().map { it.quantity })
    }

    @Test
    fun `a quantity of zero removes the line`() = runTest {
        cart.add(coffee)

        cart.setQuantity("coffee", quantity = 0)

        // Not a row holding nothing, which would draw as "0 x Coffee".
        assertEquals(emptyList<CartItem>(), items())
    }

    @Test
    fun `a negative quantity removes the line too`() = runTest {
        cart.add(coffee)

        cart.setQuantity("coffee", quantity = -1)

        assertEquals(emptyList<CartItem>(), items())
    }

    @Test
    fun `setting a quantity on something that is not in the cart adds nothing`() = runTest {
        cart.setQuantity("coffee", quantity = 2)

        // An upsert here would conjure a line with no name and no price.
        assertEquals(emptyList<CartItem>(), items())
    }

    @Test
    fun `removing takes one line out and leaves the rest`() = runTest {
        cart.add(coffee)
        cart.add(tea)

        cart.remove("coffee")

        assertEquals(listOf("Tea"), names())
    }

    @Test
    fun `clearing empties the cart`() = runTest {
        cart.add(coffee)
        cart.add(tea)

        cart.clear()

        assertEquals(emptyList<CartItem>(), items())
        assertEquals(0, cart.observeCount().first())
    }
}
