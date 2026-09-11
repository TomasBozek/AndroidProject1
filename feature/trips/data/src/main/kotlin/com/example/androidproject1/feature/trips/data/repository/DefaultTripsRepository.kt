package com.example.androidproject1.feature.trips.data.repository

import com.example.androidproject1.feature.trips.data.source.LocalTripsDataSource
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripsRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultTripsRepository(
    logger: Logger,
    private val localTripsDataSource: LocalTripsDataSource,
) : TripsRepository, BaseRepository(logger = logger.withTag("DefaultTripsRepository")) {

    // retries, because the list and the dashboard both hold this collector for as long as the
    // screen exists — one transient database error must not stop it emitting for good.
    override fun observeTrips(): Flow<Outcome<List<Trip>>> =
        observe(source = localTripsDataSource.observeTrips(), retries = RETRIES)

    override suspend fun getTrip(tripId: String): Outcome<Trip?> = execute { localTripsDataSource.getTrip(tripId) }

    override suspend fun saveTrip(trip: Trip): Outcome<Unit> = execute { localTripsDataSource.upsertTrip(trip) }

    override suspend fun deleteTrip(tripId: String): Outcome<Unit> =
        execute { localTripsDataSource.deleteTrip(tripId) }

    private companion object {

        const val RETRIES = 3L
    }
}
