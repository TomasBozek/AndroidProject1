package com.example.androidproject1.feature.movies.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The database's own shape, deliberately not the domain's.
 *
 * A row remembers the page it arrived on and its position in it (D79): a page is replaced as a
 * unit, and the list is read back in the order the server gave it rather than by anything a
 * column happens to sort on.
 */
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val page: Int,
    val position: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    /** ISO-8601, or `null` when TMDB has no date — it sends `""` for those. */
    val releaseDate: String?,
    val voteAverage: Double,
)

/**
 * What the detail endpoint adds, keyed by the movie. A row of its own rather than nullable columns
 * on [MovieEntity]: a page refresh replaces the list rows and would take the detail with them.
 */
@Entity(tableName = "movie_details")
data class MovieDetailEntity(
    @PrimaryKey val id: Int,
    val runtimeMinutes: Int?,
    val tagline: String?,
    /** Names, joined by [GENRE_SEPARATOR]; the detail is read whole, never queried by genre. */
    val genres: String,
    val backdropUrl: String?,
) {

    companion object {

        /** TMDB's genre names carry no comma, so a comma is a separator and nothing else. */
        const val GENRE_SEPARATOR = ","
    }
}

/**
 * That a page was fetched, and what the server said the last page was.
 *
 * The rows alone cannot tell "never fetched" from "fetched, and empty" — both are no rows — and
 * `cached()` needs the difference to stop refetching an empty page for ever. The marker is also
 * where `totalPages` lives, so a warm start knows the list's end without asking.
 */
@Entity(tableName = "movie_pages")
data class MoviePageEntity(
    @PrimaryKey val page: Int,
    val totalPages: Int,
    val fetchedAt: Long,
)
