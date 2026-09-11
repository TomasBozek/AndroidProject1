package com.example.androidproject1.feature.trips.data.source

import com.example.androidproject1.feature.trips.data.database.TripDao
import com.example.androidproject1.feature.trips.data.database.toDomain
import com.example.androidproject1.feature.trips.data.database.toEntity
import com.example.androidproject1.feature.trips.domain.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultLocalTripsDataSource(private val tripDao: TripDao) : LocalTripsDataSource {

    override fun observeTrips(): Flow<List<Trip>> = tripDao.observeTrips().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getTrip(tripId: String): Trip? = tripDao.getTrip(tripId)?.toDomain()

    override suspend fun upsertTrip(trip: Trip) = tripDao.upsert(trip.toEntity())

    override suspend fun deleteTrip(tripId: String) = tripDao.delete(tripId)
}
