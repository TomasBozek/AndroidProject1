package com.example.androidproject1.feature.movies.presentation.movies

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.androidproject1.feature.movies.domain.Movie
import java.time.LocalDate

/**
 * The list as drawn: every page loaded so far, flattened, and where the loading stands.
 *
 * `page` is the last page in [movies] and `totalPages` what the server said last, so the screen
 * knows whether reaching the end means "load more" or "that is all" without asking anyone.
 */
@Immutable
data class MoviesState(
    val movies: List<Movie> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 0,
    /** Page 1 has answered at least once — from the cache or the network — so empty means empty. */
    val loaded: Boolean = false,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
) {

    val endReached: Boolean get() = loaded && page >= totalPages

    companion object {

        val PREVIEW = MoviesState(
            movies = listOf(
                Movie(
                    id = 550,
                    title = "Fight Club",
                    overview = "An insomniac office worker and a soap salesman start an underground club.",
                    posterUrl = null,
                    releaseDate = LocalDate.of(1999, 10, 15),
                    rating = 8.4,
                ),
                Movie(
                    id = 27205,
                    title = "Inception",
                    overview = "A thief who steals secrets from inside dreams is offered a chance at redemption.",
                    posterUrl = null,
                    releaseDate = LocalDate.of(2010, 7, 15),
                    rating = 8.4,
                ),
                Movie(
                    id = 157336,
                    title = "Interstellar",
                    overview = "A team of explorers travels through a wormhole in search of a new home.",
                    posterUrl = null,
                    releaseDate = LocalDate.of(2014, 11, 5),
                    rating = 8.4,
                ),
            ),
            page = 1,
            totalPages = 3,
            loaded = true,
        )
    }
}

/**
 * The states this screen is drawn in — for the preview, and so for the goldens.
 *
 * Three: the list, the empty list, and the list with a page on its way, because the footer is
 * where a layout forgets to leave room.
 */
class MoviesStatePreviews : PreviewParameterProvider<MoviesState> {

    override val values = sequenceOf(
        MoviesState.PREVIEW,
        MoviesState(loaded = true),
        MoviesState.PREVIEW.copy(loadingMore = true),
    )
}
