package com.example.androidproject1.feature.movies.domain

import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the data layer. Declared here so the domain layer depends on nothing.
 *
 * A page is a cache entry (D79): [observePage] is cache-then-network for one page, so a list
 * grows by observing the next page when it reaches its end, and each page it has already seen
 * draws from the table at once.
 */
interface MoviesRepository {

    /** The cached page first, if there is one; then what the server says; then the cache again. */
    fun observePage(page: Int): Flow<Outcome<MoviePage>>

    /**
     * Fetches page 1 and, on success, makes it the whole cache — pages after it are gone until
     * scrolled to again. On failure the cache is left as it was.
     */
    suspend fun refresh(): Outcome<Unit>

    /** The cached detail, or one network round trip that is then cached. */
    suspend fun getMovie(id: Int): Outcome<MovieDetail>
}
