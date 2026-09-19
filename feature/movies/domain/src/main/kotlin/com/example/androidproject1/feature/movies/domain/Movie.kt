package com.example.androidproject1.feature.movies.domain

import java.time.LocalDate

/**
 * One row of the popular list. The wire shape is `MovieDto`, in the data layer; the poster is a
 * full URL here because the domain never learns that TMDB hands out a path and a base separately.
 */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val releaseDate: LocalDate?,
    /** TMDB's `vote_average`, 0–10. */
    val rating: Double,
)
