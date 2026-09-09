package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [CategoryEntity::class, ProductEntity::class, FavouriteEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao

    abstract fun favouritesDao(): FavouritesDao

    companion object {

        const val NAME = "catalog.db"

        /**
         * Every migration this database has, in one place because two things read it: the builder
         * in `CatalogModule` and `CatalogDatabaseMigrationTest`. A `version` bump ships its
         * migration and its entry here in the same commit — the test fails otherwise, which is
         * the point.
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
