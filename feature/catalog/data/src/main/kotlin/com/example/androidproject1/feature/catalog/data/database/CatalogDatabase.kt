package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        FavouriteEntity::class,
        CatalogFetchEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao

    abstract fun favouritesDao(): FavouritesDao

    companion object {

        const val NAME = "catalog.db"

        /**
         * Adds the fetch markers. Nothing backfills them, so every cached list reads as never
         * fetched once and is refetched on the next collection — which is what the app did before
         * the table existed, and cheaper than inventing a time for rows nobody timed.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `catalog_fetches` " +
                        "(`key` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`key`))",
                )
            }
        }

        /**
         * Every migration this database has, in one place because two things read it: the builder
         * in `CatalogModule` and `CatalogDatabaseMigrationTest`. A `version` bump ships its
         * migration and its entry here in the same commit — the test fails otherwise, which is
         * the point. Declared after the migrations it names: a companion initialises top down, so
         * an array written above them would hold nulls.
         */
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
