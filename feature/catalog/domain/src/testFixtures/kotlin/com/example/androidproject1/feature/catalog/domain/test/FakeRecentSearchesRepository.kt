package com.example.androidproject1.feature.catalog.domain.test

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.feature.catalog.domain.RecentSearchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [RecentSearchesRepository], beside the interface it fakes.
 *
 * [failWith] is settable independently of `FakeCatalogRepository`'s, which is the point of the
 * screen it serves: one of the two loads fails and the other keeps working.
 */
class FakeRecentSearchesRepository(var failWith: DomainError? = null) : RecentSearchesRepository {

    val recents = MutableStateFlow<List<String>>(emptyList())

    override fun observeRecents(): Flow<Outcome<List<String>>> =
        recents.map { stored -> failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(stored) }

    override suspend fun record(query: String): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            recents.value = listOf(trimmed) + recents.value.filterNot { it.equals(trimmed, true) }
        }
        return Outcome.Success(Unit)
    }

    override suspend fun clear(): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        recents.value = emptyList()
        return Outcome.Success(Unit)
    }
}
