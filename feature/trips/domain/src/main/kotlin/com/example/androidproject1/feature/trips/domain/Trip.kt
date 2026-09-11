package com.example.androidproject1.feature.trips.domain

import java.time.LocalDate

/** What kind of trip it is — drives nothing yet beyond the label, but every trip has to be one. */
enum class TripType {
    Leisure,
    Business,
    Family,
}

/**
 * Where a trip sits relative to today. Derived from [Trip.startDate] and [Trip.endDate] rather
 * than stored — see [Trip.status] — so it can never drift out of sync with the dates it describes.
 */
enum class TripStatus {
    Upcoming,
    Active,
    Completed,
}

/**
 * @property destinationId the [Destination] chosen in the wizard's second step, carried back from
 * [com.example.androidproject1.feature.trips.domain] as an id rather than a name: a destination
 * can be renamed without every trip that points at it going stale.
 * @property budgetMinMinor, [budgetMaxMinor] in minor units, the same convention `Product.price`
 * uses — an integer cannot drift the way a `Double` can, and formatting is `LocalFormats`' job.
 * @property travelers always at least one — the trip's own party, not a headcount of who is going
 * along for a single day of it.
 */
data class Trip(
    val id: String,
    val name: String,
    val destinationId: String,
    val destinationName: String,
    val type: TripType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val travelers: Int,
    val budgetMinMinor: Long,
    val budgetMaxMinor: Long,
    val notes: String,
) {

    /** Today decides this; nothing about the trip itself does. */
    fun status(today: LocalDate): TripStatus = when {
        today.isBefore(startDate) -> TripStatus.Upcoming
        today.isAfter(endDate) -> TripStatus.Completed
        else -> TripStatus.Active
    }
}
