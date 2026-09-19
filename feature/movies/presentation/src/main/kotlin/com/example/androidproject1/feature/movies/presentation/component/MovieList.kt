package com.example.androidproject1.feature.movies.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.androidproject1.core.ui.common.ComponentPreview
import com.example.androidproject1.core.ui.common.ThemedComponentPreview
import com.example.androidproject1.core.ui.component.AppDivider
import com.example.androidproject1.core.ui.component.AppSpinner
import com.example.androidproject1.core.ui.theme.AppTheme
import com.example.androidproject1.feature.movies.domain.Movie
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.time.LocalDate

/**
 * The rows, and the ask for more.
 *
 * The list asks for the next page a few rows before its end, once per end reached: the flow fires
 * on the transition into the last rows, and the view model ignores an ask it is already
 * answering. While a page is on its way the last row is a spinner, so the end of the list says
 * "more is coming" rather than looking like the end.
 */
@Composable
fun MovieList(
    movies: List<Movie>,
    loadingMore: Boolean,
    onMovieClick: (Movie) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LaunchedEffect(listState, movies.size) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= movies.size - PREFETCH_ROWS
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("movies_list"),
    ) {
        items(movies, key = { it.id }) { movie ->
            MovieRow(
                movie = movie,
                onClick = { onMovieClick(movie) },
                modifier = Modifier.testTag("movies_item"),
            )
            AppDivider()
        }
        if (loadingMore) {
            item(key = LOADING_KEY) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.spacing.stack.md)
                        .testTag("movies_moreProgress"),
                    contentAlignment = Alignment.Center,
                ) {
                    AppSpinner()
                }
            }
        }
    }
}

/** How many rows before the end the next page is asked for. */
private const val PREFETCH_ROWS = 3
private const val LOADING_KEY = "loading"

@ComponentPreview
@Composable
private fun Preview() = ThemedComponentPreview {
    MovieList(
        movies = List(3) { index ->
            Movie(
                id = index,
                title = "Movie ${index + 1}",
                overview = "",
                posterUrl = null,
                releaseDate = LocalDate.of(2000 + index, 1, 1),
                rating = 7.0 + index / 10.0,
            )
        },
        loadingMore = true,
        onMovieClick = {},
        onLoadMore = {},
    )
}
