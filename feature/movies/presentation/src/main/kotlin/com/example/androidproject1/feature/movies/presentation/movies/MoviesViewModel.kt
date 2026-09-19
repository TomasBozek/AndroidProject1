package com.example.androidproject1.feature.movies.presentation.movies

import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.feature.movies.domain.MoviePage
import com.example.androidproject1.feature.movies.domain.MoviesRepository
import com.example.androidproject1.feature.movies.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import kotlinx.coroutines.Job

/**
 * The list grows a page at a time (D79): page 1 is observed from the start, and every page after
 * it when the screen says it has reached the end of what it has. Each page is its own
 * `observe` over the repository's cache-then-network flow, so a page seen before draws from the
 * table at once and is refreshed behind it, and a page that fails leaves the ones before it alone.
 */
class MoviesViewModel(
    logger: Logger,
    private val moviesRepository: MoviesRepository,
) : BaseViewModel<MoviesState, MoviesEvent, MoviesNavigation>(
    // An empty list, not null: the screen draws its shell at once and the overlay covers the
    // first page's wait — which a cache hit ends on the first frame.
    initialState = MoviesState(),
    logger = logger.withTag("MoviesViewModel"),
) {

    /** What each page holds, by page number; the state is these flattened in order. */
    private val pages = sortedMapOf<Int, MoviePage>()

    /** The observation per page, so a refresh can stop the ones it made stale. */
    private val observations = mutableMapOf<Int, Job>()

    init {
        observePage(FIRST_PAGE)
    }

    override fun onUiEvent(event: MoviesEvent) {
        when (event) {
            MoviesEvent.Refresh -> refresh()
            MoviesEvent.LoadMore -> loadMore()
            is MoviesEvent.MovieClicked -> navigate(MoviesNavigation.MovieDetail(event.id))
            MoviesEvent.NavigateUpClicked -> navigate(MoviesNavigation.NavigateUp)
        }
    }

    private fun loadMore() {
        val state = uiState.value.data ?: return
        if (state.loadingMore || state.refreshing || !state.loaded || state.endReached) return
        observePage(state.page + 1)
    }

    private fun observePage(page: Int) {
        observations[page]?.cancel()
        observations[page] = observe(
            flow = { moviesRepository.observePage(page) },
            // The overlay for the page the screen opens on; a footer spinner for the rest.
            loading = if (page == FIRST_PAGE) overlay() else { loading -> updateData { copy(loadingMore = loading) } },
            // A first page with nothing cached has nowhere else to put its error; a later one
            // keeps the list and reports beside it.
            errorDisplay = if (page == FIRST_PAGE) ErrorDisplay.Inline else ErrorDisplay.Alert,
            onError = { keepStaleContent(page) },
            onData = { loaded ->
                pages[page] = loaded
                publish()
            },
        )
    }

    /** See `CategoriesViewModel.keepStaleContent`: a usable list beats an error page. */
    private fun keepStaleContent(page: Int): Boolean {
        val hasContent = page == FIRST_PAGE && pages.isNotEmpty()
        if (hasContent) showSnackbar(R.string.movies_stale.toUiText())
        return hasContent
    }

    private fun refresh() {
        if (uiState.value.data?.refreshing == true) return
        execute(
            loading = { refreshing -> updateData { copy(refreshing = refreshing) } },
            action = { moviesRepository.refresh() },
            onData = {
                // The repository dropped every page but the first; page 1's own observation sees
                // the new rows through the cache, and the others would only ever repeat theirs.
                observations.keys.filter { it > FIRST_PAGE }.forEach { observations.remove(it)?.cancel() }
                pages.keys.retainAll { it == FIRST_PAGE }
                publish()
            },
        )
    }

    private fun publish() {
        val movies: List<Movie> = pages.values.flatMap { it.movies }
        val last = pages.values.lastOrNull()
        updateData {
            copy(
                movies = movies,
                page = last?.page ?: 0,
                totalPages = last?.totalPages ?: 0,
                loaded = pages.containsKey(FIRST_PAGE),
            )
        }
    }

    private companion object {

        const val FIRST_PAGE = 1
    }
}
