package com.example.androidproject1.feature.catalog.data.repository

import com.example.androidproject1.feature.catalog.data.source.LocalRecentSearchesDataSource
import com.example.androidproject1.feature.catalog.domain.RecentSearchesRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * The list is short by construction: a recent-searches list nobody can see the bottom of is a
 * history, and this is a shortcut.
 */
class DefaultRecentSearchesRepository(
    logger: Logger,
    private val localRecentSearchesDataSource: LocalRecentSearchesDataSource,
) : RecentSearchesRepository,
    BaseRepository(logger = logger.withTag("DefaultRecentSearchesRepository")) {

    // `retries`, because the collector is a screen that stays open across a failure.
    override fun observeRecents(): Flow<Outcome<List<String>>> =
        observe(source = localRecentSearchesDataSource.observeRecents(), retries = RETRIES)

    override suspend fun record(query: String): Outcome<Unit> = execute {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@execute
        // Read, reorder, write. A repeat moves to the front instead of appearing twice, which is
        // what makes the list a shortcut rather than a log.
        val recents = localRecentSearchesDataSource.read()
            .filterNot { it.equals(trimmed, ignoreCase = true) }
        localRecentSearchesDataSource.replace((listOf(trimmed) + recents).take(LIMIT))
    }

    override suspend fun clear(): Outcome<Unit> = execute {
        localRecentSearchesDataSource.replace(emptyList())
    }

    private companion object {

        const val RETRIES = 3L

        const val LIMIT = 5
    }
}
