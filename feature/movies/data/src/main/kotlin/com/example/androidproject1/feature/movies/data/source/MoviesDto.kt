package com.example.androidproject1.feature.movies.data.source

import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.time.Duration.Companion.minutes

/**
 * TMDB's wire shapes, deliberately not the domain's: a DTO changes when the API does, and mapping
 * it here is what stops that reaching a ViewModel. Field names are TMDB's, so the fixtures under
 * `app/src/dev/res/raw` and a real answer parse the same way.
 */
@Serializable
data class MoviePageDto(
    val page: Int,
    val results: List<MovieDto>,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    /** `""` for a movie with no date, so it is a string here and a `LocalDate?` in the domain. */
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

/** `movie/{id}`: a [MovieDto] with the fields only the detail carries. */
@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    val runtime: Int? = null,
    val tagline: String? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("backdrop_path") val backdropPath: String? = null,
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)

internal fun MoviePageDto.toDomain(imageBaseUrl: String): MoviePage = MoviePage(
    page = page,
    movies = results.map { it.toDomain(imageBaseUrl) },
    totalPages = totalPages,
)

internal fun MovieDto.toDomain(imageBaseUrl: String): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { imageBaseUrl + it },
    releaseDate = releaseDate.toLocalDateOrNull(),
    rating = voteAverage,
)

internal fun MovieDetailDto.toDomain(imageBaseUrl: String): MovieDetail = MovieDetail(
    movie = Movie(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterPath?.let { imageBaseUrl + it },
        releaseDate = releaseDate.toLocalDateOrNull(),
        rating = voteAverage,
    ),
    // `0` is TMDB's "unknown", not a zero-minute film.
    runtime = runtime?.takeIf { it > 0 }?.minutes,
    tagline = tagline?.takeIf { it.isNotBlank() },
    genres = genres.map { it.name },
    backdropUrl = backdropPath?.let { imageBaseUrl + it },
)

private fun String?.toLocalDateOrNull(): LocalDate? {
    if (isNullOrBlank()) return null
    return try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
}
