package com.example.androidproject1.feature.trips.data.database

import com.example.androidproject1.feature.trips.domain.Destination
import com.example.androidproject1.feature.trips.domain.Trip
import com.example.androidproject1.feature.trips.domain.TripType
import java.time.LocalDate

internal fun TripEntity.toDomain() = Trip(
    id = id,
    name = name,
    destinationId = destinationId,
    destinationName = destinationName,
    type = TripType.valueOf(type),
    startDate = LocalDate.parse(startDate),
    endDate = LocalDate.parse(endDate),
    travelers = travelers,
    budgetMinMinor = budgetMinMinor,
    budgetMaxMinor = budgetMaxMinor,
    notes = notes,
)

internal fun Trip.toEntity() = TripEntity(
    id = id,
    name = name,
    destinationId = destinationId,
    destinationName = destinationName,
    type = type.name,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    travelers = travelers,
    budgetMinMinor = budgetMinMinor,
    budgetMaxMinor = budgetMaxMinor,
    notes = notes,
)

internal fun DestinationEntity.toDomain() = Destination(
    id = id,
    name = name,
    country = country,
    description = description,
)

internal fun Destination.toEntity() = DestinationEntity(
    id = id,
    name = name,
    country = country,
    description = description,
)
