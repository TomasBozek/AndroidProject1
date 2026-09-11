package com.example.androidproject1.feature.trips.domain.test

import com.example.androidproject1.feature.trips.domain.Destination
import com.example.androidproject1.feature.trips.domain.DestinationsRepository
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** In-memory [DestinationsRepository]. Two destinations is enough to test a picker's selection. */
class FakeDestinationsRepository(
    var destinations: List<Destination> = listOf(LISBON, PRAGUE),
    var failWith: DomainError? = null,
) : DestinationsRepository {

    override fun observeDestinations(): Flow<Outcome<List<Destination>>> =
        flowOf(failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(destinations))

    override suspend fun getDestination(destinationId: String): Outcome<Destination?> =
        failWith?.let { Outcome.Failure(it) }
            ?: Outcome.Success(destinations.find { it.id == destinationId })

    companion object {

        val LISBON = Destination(
            id = "lisbon",
            name = "Lisbon",
            country = "Portugal",
            description = "Hills, trams and the river Tagus.",
        )

        val PRAGUE = Destination(
            id = "prague",
            name = "Prague",
            country = "Czechia",
            description = "A skyline of spires over the Vltava.",
        )
    }
}
