package com.example.androidproject1.feature.movies.data.source

import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage

/**
 * Internal to the data layer: it sits beside its implementation, and nothing above
 * `:feature:movies:data` names it. What the rest of the app depends on is the repository
 * interface in `domain`.
 */
interface RemoteMoviesDataSource {

    /** `movie/popular?page=n`. A page past the last is a failure — TMDB answers `422`. */
    suspend fun getPopular(page: Int): MoviePage

    /** `movie/{id}`. */
    suspend fun getMovie(id: Int): MovieDetail
}
