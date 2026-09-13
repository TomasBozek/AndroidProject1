package com.example.androidproject1.feature.inventory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {

        const val NAME = "inventory.db"

        /**
         * Empty at version 1. Declared like `CatalogDatabase.MIGRATIONS` so a future bump has
         * somewhere to add its migration and a migration test something to walk.
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
