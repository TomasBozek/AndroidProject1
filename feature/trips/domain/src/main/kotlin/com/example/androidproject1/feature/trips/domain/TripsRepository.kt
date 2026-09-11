package com.example.androidproject1.feature.trips.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/** Implemented in the data layer. Declared here so the domain layer depends on nothing. */
interface TripsRepository {

    fun observeTrips(): Flow<Outcome<List<Trip>>>

    suspend fun getTrip(tripId: String): Outcome<Trip?>

    suspend fun saveTrip(trip: Trip): Outcome<Unit>

    suspend fun deleteTrip(tripId: String): Outcome<Unit>
}
