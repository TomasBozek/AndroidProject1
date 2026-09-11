package com.example.androidproject1.feature.trips.data.source

import com.example.androidproject1.feature.trips.domain.Destination
import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer, like [LocalTripsDataSource]: what the rest of the app depends on is
 * `DestinationsRepository` in `domain`.
 */
interface LocalDestinationsDataSource {

    fun observeDestinations(): Flow<List<Destination>>

    suspend fun getDestination(destinationId: String): Destination?
}
