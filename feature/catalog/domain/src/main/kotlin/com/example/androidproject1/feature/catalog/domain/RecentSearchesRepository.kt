package com.example.androidproject1.feature.catalog.domain

import com.example.androidproject1.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * What the user has searched for before, most recent first.
 *
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 */
interface RecentSearchesRepository {

    fun observeRecents(): Flow<Outcome<List<String>>>

    /** Records a search. A repeat moves to the front rather than being added twice. */
    suspend fun record(query: String): Outcome<Unit>

    suspend fun clear(): Outcome<Unit>
}
