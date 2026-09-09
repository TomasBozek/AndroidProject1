package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

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
    }
}
