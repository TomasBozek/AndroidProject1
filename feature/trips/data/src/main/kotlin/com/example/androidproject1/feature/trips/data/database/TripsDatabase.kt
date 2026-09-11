package com.example.androidproject1.feature.trips.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [TripEntity::class, DestinationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TripsDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    abstract fun destinationDao(): DestinationDao

    companion object {

        const val NAME = "trips.db"

        /**
         * Empty at version 1. Declared like `CatalogDatabase.MIGRATIONS` so a future bump has
         * somewhere to add its migration and `TripsDatabaseMigrationTest` something to walk.
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
