package com.example.androidproject1.feature.movies.data.repository

import com.example.androidproject1.feature.movies.data.source.LocalMoviesDataSource
import com.example.androidproject1.feature.movies.data.source.RemoteMoviesDataSource
import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import com.example.androidproject1.feature.movies.domain.MoviesRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * A page is a cache entry (D79): `cached()` per page, which is why this is wiring rather than a
 * state machine. The list above observes page 1, then page 2 when it reaches the end of page 1,
 * and each observation is cache, then network, then the cache again.
 */
class DefaultMoviesRepository(
    logger: Logger,
    private val localMoviesDataSource: LocalMoviesDataSource,
    private val remoteMoviesDataSource: RemoteMoviesDataSource,
) : MoviesRepository, BaseRepository(logger = logger.withTag("DefaultMoviesRepository")) {

    override fun observePage(page: Int): Flow<Outcome<MoviePage>> = cached(
        local = localMoviesDataSource.observePage(page),
        remote = { remoteMoviesDataSource.getPopular(page) },
        write = { localMoviesDataSource.replacePage(it) },
    )

    // Not cached(): a refresh is one round trip whose success is the whole table replaced — so
    // pages 2.. are gone until scrolled to again — and whose failure leaves it exactly as it was.
    override suspend fun refresh(): Outcome<Unit> = execute {
        localMoviesDataSource.replaceAll(remoteMoviesDataSource.getPopular(FIRST_PAGE))
    }

    // Not cached() either: the happy path is a row already listed, so a miss is one network call
    // that is then stored — a deep link to a movie no page has shown still opens.
    override suspend fun getMovie(id: Int): Outcome<MovieDetail> = execute {
        localMoviesDataSource.getDetail(id)
            ?: remoteMoviesDataSource.getMovie(id).also { localMoviesDataSource.storeDetail(it) }
    }

    private companion object {

        const val FIRST_PAGE = 1
    }
}
