package com.example.androidproject1.feature.movies.presentation.movies

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.androidproject1.core.ui.common.ScreenPreview
import com.example.androidproject1.core.ui.common.ThemedScreenPreview
import com.example.androidproject1.core.ui.component.AppEmptyState
import com.example.androidproject1.core.ui.component.AppPullToRefresh
import com.example.androidproject1.core.ui.component.AppScaffold
import com.example.androidproject1.core.ui.component.AppTopBar
import com.example.androidproject1.feature.movies.presentation.R
import com.example.androidproject1.feature.movies.presentation.component.MovieList

@Composable
fun MoviesScreen(
    state: MoviesState,
    onEvent: (MoviesEvent) -> Unit,
) {
    AppScaffold(
        screenId = "MoviesScreen",
        topBar = {
            AppTopBar(
                title = stringResource(R.string.movies_title),
                onNavigateUp = { onEvent(MoviesEvent.NavigateUpClicked) },
                navigateUpTestTag = "movies_upButton",
            )
        },
        contentPadding = false,
    ) {
        AppPullToRefresh(
            refreshing = state.refreshing,
            onRefresh = { onEvent(MoviesEvent.Refresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                state.loaded && state.movies.isEmpty() -> AppEmptyState(
                    title = stringResource(R.string.movies_empty_title),
                    message = stringResource(R.string.movies_empty_message),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("movies_empty"),
                )

                else -> MovieList(
                    movies = state.movies,
                    loadingMore = state.loadingMore,
                    onMovieClick = { onEvent(MoviesEvent.MovieClicked(it.id)) },
                    onLoadMore = { onEvent(MoviesEvent.LoadMore) },
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun Preview(
    @PreviewParameter(MoviesStatePreviews::class) state: MoviesState,
) = ThemedScreenPreview {
    MoviesScreen(
        state = state,
    ) {}
}
