package com.example.androidproject1.feature.movies.domain

/**
 * One page of the popular list — the unit the cache stores and the list appends (D79).
 *
 * `totalPages` rides on every page because the caller reading page `n` is the one that decides
 * whether there is an `n + 1`, and the server says so on each answer.
 */
data class MoviePage(
    val page: Int,
    val movies: List<Movie>,
    val totalPages: Int,
) {

    val isLast: Boolean get() = page >= totalPages
}
