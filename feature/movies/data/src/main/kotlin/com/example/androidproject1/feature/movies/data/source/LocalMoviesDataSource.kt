package com.example.androidproject1.feature.movies.data.source

import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:movies:data` names it. What the rest of the app depends on is the repository
 * interface in `domain`.
 */
interface LocalMoviesDataSource {

    /** `null` until the page has been fetched once; a page — empty or not — from then on. */
    fun observePage(page: Int): Flow<MoviePage?>

    /** Replaces that one page's rows and marker. */
    suspend fun replacePage(page: MoviePage)

    /** Replaces every page with this one — the refresh. */
    suspend fun replaceAll(page: MoviePage)

    /** The detail and the row it belongs to, or `null` when either is missing. */
    suspend fun getDetail(id: Int): MovieDetail?

    /** The detail, and the row too, so a deep link to a movie no page has listed still caches. */
    suspend fun storeDetail(detail: MovieDetail)
}
