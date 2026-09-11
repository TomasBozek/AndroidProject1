package com.example.androidproject1.feature.trips.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The database's own shape, deliberately not the domain's — see `CategoryEntity` in `:feature:catalog`
 * for why. [status] is not a column: `Trip.status(today)` derives it from the dates, so it can
 * never read stale against a clock that keeps moving.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val name: String,
    val destinationId: String,
    val destinationName: String,
    val type: String,
    val startDate: String,
    val endDate: String,
    val travelers: Int,
    val budgetMinMinor: Long,
    val budgetMaxMinor: Long,
    val notes: String,
)

/** Seeded once from `DestinationFixtures` — see `DefaultLocalDestinationsDataSource`. */
@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val description: String,
)
