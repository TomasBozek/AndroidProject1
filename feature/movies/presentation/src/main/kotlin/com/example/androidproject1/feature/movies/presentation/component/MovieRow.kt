package com.example.androidproject1.feature.movies.presentation.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppImage
import com.example.androidproject1.core.ui.component.AppListItem
import com.example.androidproject1.core.ui.layout.appSharedElement
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.service.core.ui.format.LocalFormats
import java.time.LocalDate

/**
 * One movie in the list: the poster, the title, and the release date beside the rating.
 *
 * A list row rather than a card, so sixty of them read as a list and the poster is the row's
 * leading slot — a 2:3 box one row tall, which is what a poster is shaped like. The rating is
 * TMDB's 0–10 shown as a percentage, so it reads the same in every locale without a formatter
 * the design system does not have.
 */
@Composable
fun MovieRow(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
) {
    val formats = LocalFormats.current
    val supporting = listOfNotNull(
        movie.releaseDate?.let(formats::date),
        formats.percent(movie.rating / RATING_SCALE),
    ).joinToString(SEPARATOR)
    AppListItem(
        headline = movie.title,
        supporting = supporting,
        onClick = onClick,
        modifier = modifier,
        leading = {
            AppImage(
                model = movie.posterUrl,
                contentDescription = null,
                shape = AppTheme.shapes.sm,
                // Travels to the detail's poster (D76); a no-op wherever no transition runs.
                modifier = posterModifier
                    .height(AppTheme.density.listRowHeight)
                    .aspectRatio(POSTER_RATIO)
                    .appSharedElement(moviePosterKey(movie.id)),
            )
        },
    )
}

private const val RATING_SCALE = 10.0
private const val POSTER_RATIO = 2f / 3f
private const val SEPARATOR = " · "

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    MovieRow(
        movie = Movie(
            id = 550,
            title = "Fight Club",
            overview = "",
            posterUrl = null,
            releaseDate = LocalDate.of(1999, 10, 15),
            rating = 8.4,
        ),
        onClick = {},
    )
    MovieRow(
        movie = Movie(
            id = 1,
            title = "A title long enough to wrap onto a second line in a narrow row",
            overview = "",
            posterUrl = null,
            releaseDate = null,
            rating = 0.0,
        ),
        onClick = {},
    )
}
