package com.example.androidproject1.feature.trips.data.source

import com.example.androidproject1.feature.trips.data.database.DestinationDao
import com.example.androidproject1.feature.trips.data.database.toDomain
import com.example.androidproject1.feature.trips.data.database.toEntity
import com.example.androidproject1.feature.trips.domain.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Seeds [DESTINATION_FIXTURES] into the table the first time anything asks — never on every
 * launch, because `INSERT OR IGNORE` against a primary key would make that a silent no-op anyway,
 * but the `count()` check keeps a full picker open with the app for the rest of the process from
 * re-running eight inserts it already knows are pointless.
 */
class DefaultLocalDestinationsDataSource(private val destinationDao: DestinationDao) : LocalDestinationsDataSource {

    private var seeded = false

    // Seeded *before* subscribing to the query: Room's Flow only replays what the table held at
    // subscription time and then re-queries on the next write, so seeding from inside a `map` on
    // this flow would let the very first collection through with an empty table — exactly what a
    // caller that reads only the first emission, such as a picker opened once, would see.
    override fun observeDestinations(): Flow<List<Destination>> = flow {
        ensureSeeded()
        emitAll(destinationDao.observeDestinations().map { rows -> rows.map { it.toDomain() } })
    }

    override suspend fun getDestination(destinationId: String): Destination? {
        ensureSeeded()
        return destinationDao.getDestination(destinationId)?.toDomain()
    }

    private suspend fun ensureSeeded() {
        if (seeded) return
        if (destinationDao.count() == 0) {
            destinationDao.insertAll(DESTINATION_FIXTURES.map { it.toEntity() })
        }
        seeded = true
    }
}
