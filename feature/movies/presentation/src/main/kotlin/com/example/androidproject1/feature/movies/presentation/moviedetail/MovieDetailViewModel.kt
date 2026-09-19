package com.example.androidproject1.feature.movies.presentation.moviedetail

import com.example.androidproject1.feature.movies.domain.MoviesRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import kotlinx.coroutines.flow.update

class MovieDetailViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: MovieDetailDestination,
    private val moviesRepository: MoviesRepository,
) : BaseViewModel<MovieDetailState, MovieDetailEvent, MovieDetailNavigation>(
    // Nothing to show until the movie named by the route has loaded: the id alone draws nothing.
    initialState = null,
    logger = logger.withTag("MovieDetailViewModel"),
) {

    init {
        load()
    }

    override fun onUiEvent(event: MovieDetailEvent) = when (event) {
        MovieDetailEvent.NavigateUpClicked -> navigate(MovieDetailNavigation.NavigateUp)
    }

    // One read, cached or not — the repository decides. Inline: with nothing on screen, an error
    // has nowhere else to go, and the retry it offers re-runs exactly this.
    private fun load() = execute(
        loading = overlay(),
        errorDisplay = ErrorDisplay.Inline,
        action = { moviesRepository.getMovie(args.movieId) },
        onData = { detail -> uiState.update { it.copy(data = MovieDetailState(detail = detail)) } },
    )
}
