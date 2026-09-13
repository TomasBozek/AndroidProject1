package com.example.androidproject1.feature.trips.data.database

import androidx.room.TypeConverter
import com.example.androidproject1.feature.trips.domain.TripType
import java.time.LocalDate

/**
 * How a non-primitive column is stored: as `TEXT`, so the schema is what it was when the columns
 * were strings and the version stays 1. The entity carries the domain type and the mapper stops
 * parsing, which is where a bad row used to throw — inside the `map` of a Room `Flow`, where
 * `observe()` gave up after its retries and the whole list read as an error because of one row.
 */
internal class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(text: String): LocalDate = LocalDate.parse(text)

    @TypeConverter
    fun fromTripType(type: TripType): String = type.name

    /**
     * A name the code no longer has — a constant renamed in a later version, or a hand-edited
     * row — reads as [TripType.Leisure] rather than throwing. No migration could fix it, because
     * the schema did not change, and a trip with the wrong type is still a trip; a list that
     * fails to load because of one is not.
     */
    @TypeConverter
    fun toTripType(name: String): TripType = TripType.entries.firstOrNull { it.name == name } ?: TripType.Leisure
}
