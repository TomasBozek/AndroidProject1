package com.example.androidproject1.feature.movies.presentation.movies

import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel

class MoviesViewModel(
    logger: Logger,
) : BaseViewModel<MoviesState, MoviesEvent, MoviesNavigation>(
    // Pass null instead when the screen cannot render until something is loaded; the loading
    // overlay then stays up until the ViewModel puts a state in.
    initialState = MoviesState.PREVIEW,
    logger = logger.withTag("MoviesViewModel"),
) {

    // What the user did arrives here; emit a MoviesNavigation to move on. Anything that can
    // fail goes through execute {} or observe(flow = …) {} — never try/catch.
    override fun onUiEvent(event: MoviesEvent) = when (event) {
        MoviesEvent.IncrementClicked -> updateData { copy(counter = counter + 1) }
    }
}
