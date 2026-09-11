package com.example.androidproject1.feature.trips.data.source

import com.example.androidproject1.feature.trips.domain.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer: what the rest of the app depends on is `TripsRepository` in
 * `domain`.
 */
interface LocalTripsDataSource {

    fun observeTrips(): Flow<List<Trip>>

    suspend fun getTrip(tripId: String): Trip?

    suspend fun upsertTrip(trip: Trip)

    suspend fun deleteTrip(tripId: String)
}
