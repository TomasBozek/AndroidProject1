package com.example.androidproject1.feature.movies.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailEntity::class,
        MoviePageEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MoviesDatabase : RoomDatabase() {

    abstract fun moviesDao(): MoviesDao

    companion object {

        const val NAME = "movies.db"

        /**
         * Every migration this database has, in one place because two things read it: the builder
         * in `MoviesModule` and `MoviesDatabaseMigrationTest`. A `version` bump ships its migration
         * and its entry here in the same commit — the test fails otherwise, which is the point.
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
