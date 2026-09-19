package com.example.androidproject1.feature.movies.data.source

import com.example.androidproject1.feature.movies.data.database.MoviePageEntity
import com.example.androidproject1.feature.movies.data.database.MoviesDao
import com.example.androidproject1.feature.movies.data.database.toDomain
import com.example.androidproject1.feature.movies.data.database.toEntities
import com.example.androidproject1.feature.movies.data.database.toEntity
import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The movies, read from and written to the database, one page at a time (D79).
 *
 * A page reads as `null` until it has been fetched and as a [MoviePage] from then on; the rows
 * alone cannot tell the two apart, so the page marker written with them does — and carries the
 * `totalPages` the server said, so a warm start knows where the list ends.
 */
class DefaultLocalMoviesDataSource(
    private val moviesDao: MoviesDao,
    private val now: () -> Long = System::currentTimeMillis,
) : LocalMoviesDataSource {

    override fun observePage(page: Int): Flow<MoviePage?> =
        moviesDao.observePage(page).combine(moviesDao.observePageMarker(page)) { rows, marker ->
            if (marker == null) {
                null
            } else {
                MoviePage(page = page, movies = rows.map { it.toDomain() }, totalPages = marker.totalPages)
            }
        }

    override suspend fun replacePage(page: MoviePage) =
        moviesDao.replacePage(page.page, page.toEntities(), page.marker())

    override suspend fun replaceAll(page: MoviePage) = moviesDao.replaceAll(page.toEntities(), page.marker())

    override suspend fun getDetail(id: Int): MovieDetail? {
        val detail = moviesDao.detail(id) ?: return null
        val movie = moviesDao.movie(id) ?: return null
        return detail.toDomain(movie.toDomain())
    }

    override suspend fun storeDetail(detail: MovieDetail) {
        // A row for a movie no page listed sits on page 0, position 0: never observed by a page,
        // there only so the detail can be joined back onto it.
        if (moviesDao.movie(detail.movie.id) == null) {
            moviesDao.insertMovie(detail.movie.toEntity(page = 0, position = 0))
        }
        moviesDao.insertDetail(detail.toEntity())
    }

    private fun MoviePage.marker() = MoviePageEntity(page = page, totalPages = totalPages, fetchedAt = now())
}
