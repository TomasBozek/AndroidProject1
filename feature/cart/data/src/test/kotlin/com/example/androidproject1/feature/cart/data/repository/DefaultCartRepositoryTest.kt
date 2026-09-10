package com.example.androidproject1.feature.cart.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.cart.data.database.CartDatabase
import com.example.androidproject1.feature.cart.data.source.DefaultLocalCartDataSource
import com.example.androidproject1.feature.cart.data.source.LocalCartDataSource
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.domain.test.FakeLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/** Robolectric ships an SDK image per API level and has none for this project's targetSdk. */

private val COFFEE = CartItem("coffee", "Coffee", price = 450, quantity = 1)

/**
 * The cart repository over a real SQLite.
 *
 * Its own body is an [Outcome] wrapper, so what is worth asserting is what the wrapper does at the
 * edges: that a database error arrives as a failure rather than as a crash, and that a transient
 * one does not silently end the flow the tab badge lives on.
 */
@RunWith(RobolectricTestRunner::class)
class DefaultCartRepositoryTest {

    private lateinit var database: CartDatabase

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
    }

    @After
    fun tearDown() = database.close()

    private fun repository(
        source: LocalCartDataSource = DefaultLocalCartDataSource(database.cartDao()) { 1_000L },
    ) = DefaultCartRepository(logger = FakeLogger(), localCartDataSource = source)

    @Test
    fun `an added product reaches the table and comes back as a success`() = runTest {
        val repository = repository()

        assertTrue(repository.add(COFFEE) is Outcome.Success)

        assertEquals(listOf(COFFEE), (repository.observeItems().first() as Outcome.Success).data)
        assertEquals(1, (repository.observeCount().first() as Outcome.Success).data)
    }

    @Test
    fun `clearing empties the cart and the badge`() = runTest {
        val repository = repository()
        repository.add(COFFEE.copy(quantity = 4))

        repository.clear()

        assertEquals(0, (repository.observeCount().first() as Outcome.Success).data)
    }

    @Test
    fun `a read failure is reported rather than thrown`() = runTest {
        val repository = repository(FlakyLocalCartDataSource(readFailures = Int.MAX_VALUE))

        assertTrue(repository.observeItems().first() is Outcome.Failure)
        assertTrue(repository.observeCount().first() is Outcome.Failure)
    }

    @Test
    fun `a transient read failure does not end the badge flow`() = runTest {
        // The badge is collected for as long as the main flow is on screen. Without the retries
        // the repository asks for, one unlucky read would freeze it for the rest of the session.
        val source = FlakyLocalCartDataSource(readFailures = 2)
        val repository = repository(source)
        repository.add(COFFEE.copy(quantity = 2))

        assertEquals(2, (repository.observeCount().first() as Outcome.Success).data)
    }

    @Test
    fun `a write failure is reported rather than thrown`() = runTest {
        val repository = repository(FlakyLocalCartDataSource(failWrites = true))

        assertTrue(repository.add(COFFEE) is Outcome.Failure)
        assertTrue(repository.setQuantity("coffee", 2) is Outcome.Failure)
        assertTrue(repository.remove("coffee") is Outcome.Failure)
        assertTrue(repository.clear() is Outcome.Failure)
    }
}

/**
 * A source that fails a fixed number of times before working.
 *
 * A source that always fails cannot tell a retried flow from an abandoned one, which is the whole
 * question `retries` exists to answer.
 */
private class FlakyLocalCartDataSource(
    private val readFailures: Int = 0,
    private val failWrites: Boolean = false,
) : LocalCartDataSource {

    private val stored = MutableStateFlow<List<CartItem>>(emptyList())

    private var reads = 0

    override fun observeItems(): Flow<List<CartItem>> = flow {
        if (reads++ < readFailures) throw IOException("the database is locked")
        emitAll(stored)
    }

    override fun observeCount(): Flow<Int> =
        observeItems().map { items -> items.sumOf { it.quantity } }

    override suspend fun add(item: CartItem) = write { stored.value = stored.value + item }

    override suspend fun setQuantity(productId: String, quantity: Int) = write {
        stored.value = stored.value.map {
            if (it.productId == productId) it.copy(quantity = quantity) else it
        }
    }

    override suspend fun remove(productId: String) =
        write { stored.value = stored.value.filterNot { it.productId == productId } }

    override suspend fun clear() = write { stored.value = emptyList() }

    private inline fun write(block: () -> Unit) {
        if (failWrites) throw IOException("the disk is full")
        block()
    }
}
