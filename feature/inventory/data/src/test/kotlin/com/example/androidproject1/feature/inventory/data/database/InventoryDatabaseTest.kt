package com.example.androidproject1.feature.inventory.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.inventory.data.source.DefaultLocalInventoryDataSource
import com.example.androidproject1.feature.inventory.domain.Item
import com.example.androidproject1.feature.inventory.domain.ItemCategory
import com.example.androidproject1.feature.inventory.domain.ItemCondition
import com.example.androidproject1.feature.inventory.domain.ItemTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

/**
 * The DAO round trip against a real SQLite, and the seeding — the fixtures arrive once and are
 * never put back over what the user changed. See `TripsDatabaseTest` for the pattern.
 */
@RunWith(RobolectricTestRunner::class)
class InventoryDatabaseTest {

    private lateinit var database: InventoryDatabase
    private lateinit var items: DefaultLocalInventoryDataSource

    private val lamp = Item(
        id = "item-lamp",
        name = "Desk lamp",
        category = ItemCategory.Furniture,
        condition = ItemCondition.Good,
        quantity = 1,
        priceMinor = 790_00,
        acquiredOn = LocalDate.of(2026, 1, 9),
        insured = true,
        tags = setOf(ItemTag.Fragile, ItemTag.Favourite),
        owner = "Eva Dvořáková",
        imageUrl = null,
        notes = "Warm bulb.",
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            InventoryDatabase::class.java,
        )
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .allowMainThreadQueries()
            .build()
        items = DefaultLocalInventoryDataSource(database.itemDao())
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `the fixtures seed once, on first read`() = runTest {
        val first = items.observeItems().first()

        assertEquals(FIXTURE_COUNT, first.size)

        // A second read must not re-insert — the seed is a starting point, not a refresh.
        items.deleteItems(setOf(first.first().id))
        assertEquals(FIXTURE_COUNT - 1, items.observeItems().first().size)
    }

    @Test
    fun `a saved item is readable and round trips every field, the tag set included`() = runTest {
        items.upsertItem(lamp)

        assertEquals(lamp, items.getItem("item-lamp"))
        assertEquals(lamp, items.observeItem("item-lamp").first())
    }

    @Test
    fun `saving again with the same id replaces rather than duplicates`() = runTest {
        items.upsertItem(lamp)
        val before = items.observeItems().first().size

        items.upsertItem(lamp.copy(quantity = 3))

        assertEquals(3, items.getItem("item-lamp")?.quantity)
        assertEquals(before, items.observeItems().first().size)
    }

    @Test
    fun `deleting several ids removes them all`() = runTest {
        items.upsertItem(lamp)
        items.upsertItem(lamp.copy(id = "item-lamp-2"))

        items.deleteItems(setOf("item-lamp", "item-lamp-2"))

        assertNull(items.getItem("item-lamp"))
        assertNull(items.observeItem("item-lamp-2").first())
    }

    @Test
    fun `a stored category or tag the code no longer has reads as an item, not a crash`() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO items (id, name, category, condition, quantity, priceMinor, acquiredOn, insured, tags,
                owner, imageUrl, notes)
            VALUES ('item-odd', 'Odd one', 'Vehicles', 'Mint', 1, 100, NULL, 0, 'Fragile,Haunted', 'Nobody',
                NULL, '')
            """.trimIndent(),
        )

        val item = items.getItem("item-odd")

        assertEquals(ItemCategory.Other, item?.category)
        assertEquals(ItemCondition.Good, item?.condition)
        assertEquals(setOf(ItemTag.Fragile), item?.tags)
    }

    private companion object {

        const val FIXTURE_COUNT = 12
    }
}
