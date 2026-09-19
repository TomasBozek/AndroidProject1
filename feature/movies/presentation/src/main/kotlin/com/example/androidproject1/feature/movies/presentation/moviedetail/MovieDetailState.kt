package com.example.androidproject1.feature.movies.presentation.moviedetail

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.feature.movies.domain.MovieDetail
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

@Immutable
data class MovieDetailState(
    val detail: MovieDetail,
) {

    companion object {

        val PREVIEW = MovieDetailState(
            detail = MovieDetail(
                movie = Movie(
                    id = 550,
                    title = "Fight Club",
                    overview = "An insomniac office worker and a soap salesman start an underground club that grows " +
                        "into something neither can control.",
                    posterUrl = null,
                    releaseDate = LocalDate.of(1999, 10, 15),
                    rating = 8.4,
                ),
                runtime = 139.minutes,
                tagline = "Mischief. Mayhem. Soap.",
                genres = listOf("Drama", "Thriller"),
                backdropUrl = null,
            ),
        )
    }
}

/**
 * The states this screen is drawn in — for the preview, and so for the goldens: the full detail,
 * one TMDB knows almost nothing about, and one whose every field is long.
 */
class MovieDetailStatePreviews : PreviewParameterProvider<MovieDetailState> {

    override val values = sequenceOf(
        MovieDetailState.PREVIEW,
        MovieDetailState(
            detail = MovieDetail(
                movie = MovieDetailState.PREVIEW.detail.movie.copy(overview = "", releaseDate = null, rating = 0.0),
                runtime = null,
                tagline = null,
                genres = emptyList(),
                backdropUrl = null,
            ),
        ),
        MovieDetailState(
            detail = MovieDetailState.PREVIEW.detail.copy(
                movie = MovieDetailState.PREVIEW.detail.movie.copy(
                    title = "A title long enough to wrap onto a second line and then onto a third one too",
                    overview = MovieDetailState.PREVIEW.detail.movie.overview.repeat(4),
                ),
                tagline = "A tagline that runs on and on, the way a studio's copy does when nobody stops it.",
                genres = listOf(
                    "Action",
                    "Adventure",
                    "Animation",
                    "Comedy",
                    "Crime",
                    "Documentary",
                    "Drama",
                    "Family",
                ),
            ),
        ),
    )
}
