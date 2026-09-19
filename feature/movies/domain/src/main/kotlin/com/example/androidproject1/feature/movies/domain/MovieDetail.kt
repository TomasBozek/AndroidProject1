package com.example.androidproject1.feature.movies.domain

import kotlin.time.Duration

/** What the detail endpoint adds to a [Movie]: the fields the list never carries. */
data class MovieDetail(
    val movie: Movie,
    val runtime: Duration?,
    val tagline: String?,
    val genres: List<String>,
    val backdropUrl: String?,
)
