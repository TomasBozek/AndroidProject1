package com.example.androidproject1.feature.trips.data.repository

import com.example.androidproject1.feature.trips.data.source.LocalDestinationsDataSource
import com.example.androidproject1.feature.trips.domain.Destination
import com.example.androidproject1.feature.trips.domain.DestinationsRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultDestinationsRepository(
    logger: Logger,
    private val localDestinationsDataSource: LocalDestinationsDataSource,
) : DestinationsRepository, BaseRepository(logger = logger.withTag("DefaultDestinationsRepository")) {

    override fun observeDestinations(): Flow<Outcome<List<Destination>>> =
        observe(source = localDestinationsDataSource.observeDestinations(), retries = RETRIES)

    override suspend fun getDestination(destinationId: String): Outcome<Destination?> =
        execute { localDestinationsDataSource.getDestination(destinationId) }

    private companion object {

        const val RETRIES = 3L
    }
}
