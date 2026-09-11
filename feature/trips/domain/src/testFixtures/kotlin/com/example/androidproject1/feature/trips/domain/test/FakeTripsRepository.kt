package com.example.androidproject1.feature.trips.domain.test

import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripType
import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * In-memory [TripsRepository], backed by a `MutableStateFlow` so a test that saves or deletes a
 * trip sees an observing collector react the way Room would.
 */
class FakeTripsRepository(
    initial: List<Trip> = listOf(LISBON_TRIP),
    var failWith: DomainError? = null,
) : TripsRepository {

    private val trips = MutableStateFlow(initial)

    val current: List<Trip> get() = trips.value

    var saved: Trip? = null
        private set

    var deletedId: String? = null
        private set

    override fun observeTrips(): Flow<Outcome<List<Trip>>> =
        trips.map { failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(it) }

    override suspend fun getTrip(tripId: String): Outcome<Trip?> {
        failWith?.let { return Outcome.Failure(it) }
        return Outcome.Success(trips.value.find { it.id == tripId })
    }

    override suspend fun saveTrip(trip: Trip): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        saved = trip
        trips.value = listOf(trip) + trips.value.filterNot { it.id == trip.id }
        return Outcome.Success(Unit)
    }

    override suspend fun deleteTrip(tripId: String): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        deletedId = tripId
        trips.value = trips.value.filterNot { it.id == tripId }
        return Outcome.Success(Unit)
    }

    companion object {

        // Dates ahead of the fixed clock most ViewModel tests use ("2026-09-11") so the trip
        // reads as Upcoming rather than Completed — see Trip.status().
        val LISBON_TRIP = Trip(
            id = "trip-lisbon",
            name = "Summer in Lisbon",
            destinationId = "lisbon",
            destinationName = "Lisbon, Portugal",
            type = TripType.Leisure,
            startDate = LocalDate.of(2026, 10, 10),
            endDate = LocalDate.of(2026, 10, 17),
            travelers = 2,
            budgetMinMinor = 4_000_00,
            budgetMaxMinor = 8_000_00,
            notes = "",
        )
    }
}
