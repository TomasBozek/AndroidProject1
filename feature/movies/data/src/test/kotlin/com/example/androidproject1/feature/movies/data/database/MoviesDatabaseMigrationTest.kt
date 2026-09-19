package com.example.androidproject1.feature.movies.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// A name of its own, so this never opens the database the app or the DAO test uses.
private const val DATABASE_NAME = "movies-migration-test.db"

/**
 * Every committed schema of the movies database migrates into the current one.
 *
 * This is the half a DAO test cannot reach. `MoviesDatabaseTest` builds the current schema from
 * scratch, which is what a fresh install does and what every test machine does; a user who already
 * has the app runs the migration instead. The pages are replaced on every refresh and could survive
 * being dropped; a version bump that forgets its migration still takes the offline cache with it,
 * on the one device where the cache was the point.
 *
 * Nothing here is written per version. The current version is read off the compiled database and
 * the old ones off the committed schemas, so bumping `version` without writing the migration fails
 * this test rather than needing someone to remember to extend it.
 */
@RunWith(RobolectricTestRunner::class)
class MoviesDatabaseMigrationTest {

    // Robolectric rather than an instrumentation test: the schemas are on the unit test's assets
    // — see `convention.android.room` — so `./gradlew test` covers this and CI needs no emulator.
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MoviesDatabase::class.java,
    )

    /** What the entities compile to today. `@Database` is not a runtime annotation, so it is read
     * off a database rather than off the class. */
    private val currentVersion: Int
        get() {
            val database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                MoviesDatabase::class.java,
            ).allowMainThreadQueries().build()
            return try {
                database.openHelper.readableDatabase.version
            } finally {
                database.close()
            }
        }

    /** The versions under `schemas/`, which Room exports on every compile. */
    private val committedVersions: List<Int>
        get() {
            val directory = requireNotNull(MoviesDatabase::class.qualifiedName)
            val assets = InstrumentationRegistry.getInstrumentation().context.assets
            return assets.list(directory).orEmpty()
                .mapNotNull { it.removeSuffix(".json").toIntOrNull() }
                .sorted()
        }

    @Test
    fun `the current version's schema is committed`() {
        // Room exports it on compile, so this only fails when the directory was left out of the
        // commit — and then the migration below has nothing to migrate from on anyone else's
        // machine.
        assertEquals(currentVersion, committedVersions.lastOrNull())
    }

    @Test
    fun `every committed version migrates to the current schema`() {
        val versions = committedVersions
        assertTrue("no committed schema to migrate from", versions.isNotEmpty())

        for (from in versions.dropLast(1)) {
            helper.createDatabase(DATABASE_NAME, from).close()
            // validateDroppedTables: a migration that leaves the old table behind still answers
            // every query correctly and is still a bug — the data is in two places from then on.
            helper.runMigrationsAndValidate(
                DATABASE_NAME,
                currentVersion,
                true,
                *MoviesDatabase.MIGRATIONS,
            ).close()
        }
    }

    @Test
    fun `the oldest committed schema still opens`() {
        // Creating it is the assertion: MigrationTestHelper validates what it opened against the
        // committed JSON, so a schema file that no longer describes a database Room can build
        // fails here rather than on somebody's phone.
        helper.createDatabase(DATABASE_NAME, committedVersions.first()).close()
    }
}
