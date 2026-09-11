package com.example.androidproject1.feature.trips.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the data layer. Every destination the picker can offer, seeded once into the
 * local table — see `DestinationFixtures` — and read from there ever after.
 */
interface DestinationsRepository {

    fun observeDestinations(): Flow<Outcome<List<Destination>>>

    suspend fun getDestination(destinationId: String): Outcome<Destination?>
}
