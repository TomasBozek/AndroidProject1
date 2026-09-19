package com.example.androidproject1.feature.movies.data.database

import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

internal fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    releaseDate = releaseDate?.let(LocalDate::parse),
    rating = voteAverage,
)

internal fun Movie.toEntity(page: Int, position: Int): MovieEntity = MovieEntity(
    id = id,
    page = page,
    position = position,
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    releaseDate = releaseDate?.toString(),
    voteAverage = rating,
)

internal fun MoviePage.toEntities(): List<MovieEntity> =
    movies.mapIndexed { position, movie -> movie.toEntity(page = page, position = position) }

internal fun MovieDetailEntity.toDomain(movie: Movie): MovieDetail = MovieDetail(
    movie = movie,
    runtime = runtimeMinutes?.minutes,
    tagline = tagline,
    genres = genres.split(MovieDetailEntity.GENRE_SEPARATOR).filter { it.isNotEmpty() },
    backdropUrl = backdropUrl,
)

internal fun MovieDetail.toEntity(): MovieDetailEntity = MovieDetailEntity(
    id = movie.id,
    runtimeMinutes = runtime?.inWholeMinutes?.toInt(),
    tagline = tagline,
    genres = genres.joinToString(MovieDetailEntity.GENRE_SEPARATOR),
    backdropUrl = backdropUrl,
)
