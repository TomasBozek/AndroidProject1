package com.example.androidproject1.feature.trips.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.trips.data.source.DefaultLocalDestinationsDataSource
import com.example.androidproject1.feature.trips.data.source.DefaultLocalTripsDataSource
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripType
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
 * The DAO round trip, against a real SQLite rather than a fake — see `CatalogDatabaseTest` for
 * why. What is worth testing here is the seeding: destinations arrive once, from
 * `DESTINATION_FIXTURES`, and never again.
 */
@RunWith(RobolectricTestRunner::class)
class TripsDatabaseTest {

    private lateinit var database: TripsDatabase
    private lateinit var trips: DefaultLocalTripsDataSource
    private lateinit var destinations: DefaultLocalDestinationsDataSource

    private val lisbonTrip = Trip(
        id = "trip-lisbon",
        name = "Summer in Lisbon",
        destinationId = "lisbon",
        destinationName = "Lisbon",
        type = TripType.Leisure,
        startDate = LocalDate.of(2026, 7, 10),
        endDate = LocalDate.of(2026, 7, 17),
        travelers = 2,
        budgetMinMinor = 4_000_00,
        budgetMaxMinor = 8_000_00,
        notes = "Book the tram tour",
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TripsDatabase::class.java,
        )
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .allowMainThreadQueries()
            .build()
        trips = DefaultLocalTripsDataSource(database.tripDao())
        destinations = DefaultLocalDestinationsDataSource(database.destinationDao())
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `a saved trip is readable and round trips its fields`() = runTest {
        trips.upsertTrip(lisbonTrip)

        assertEquals(lisbonTrip, trips.getTrip("trip-lisbon"))
        assertEquals(listOf(lisbonTrip), trips.observeTrips().first())
    }

    @Test
    fun `saving again with the same id replaces rather than duplicates`() = runTest {
        trips.upsertTrip(lisbonTrip)

        trips.upsertTrip(lisbonTrip.copy(travelers = 4))

        assertEquals(4, trips.getTrip("trip-lisbon")?.travelers)
        assertEquals(1, trips.observeTrips().first().size)
    }

    @Test
    fun `deleting a trip removes it`() = runTest {
        trips.upsertTrip(lisbonTrip)

        trips.deleteTrip("trip-lisbon")

        assertNull(trips.getTrip("trip-lisbon"))
        assertEquals(emptyList<Trip>(), trips.observeTrips().first())
    }

    @Test
    fun `destinations seed once, on first read`() = runTest {
        val first = destinations.observeDestinations().first()

        assertEquals(DESTINATION_COUNT, first.size)
        assertEquals("Lisbon", destinations.getDestination("lisbon")?.name)

        // A second read must not re-insert — the seed is a one-time fixture, not a refresh.
        val second = destinations.observeDestinations().first()
        assertEquals(first, second)
    }

    private companion object {

        const val DESTINATION_COUNT = 8
    }
}
