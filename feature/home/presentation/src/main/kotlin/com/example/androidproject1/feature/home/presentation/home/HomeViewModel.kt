package com.example.androidproject1.feature.home.presentation.home

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import com.example.androidproject1.feature.home.presentation.R
import kotlinx.coroutines.flow.update

class HomeViewModel(
    logger: Logger,
    private val favouritesRepository: FavouritesRepository,
) : BaseViewModel<HomeState, HomeEvent, HomeNavigation>(
    initialState = HomeState(greeting = R.string.home_greeting.toUiText()),
    logger = logger.withTag("HomeViewModel"),
) {

    companion object {

        /** Its own id, so the undo action is told apart from any other snackbar this screen shows. */
        const val SNACKBAR_UNDO_REMOVE = "home_undo_remove_favourite"
    }

    /**
     * The product the last remove took away.
     *
     * Held here rather than in the state because it is not something the screen draws — and
     * because the snackbar's action comes back as plain data (`SnackbarAction(id)`), so the
     * ViewModel has to remember what that id referred to.
     */
    private var lastRemoved: String? = null

    init {
        observeFavourites()
    }

    override fun onUiEvent(event: HomeEvent) = when (event) {
        is HomeEvent.FavouriteRemoved -> removeFavourite(event.productId)
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.SnackbarAction && event.id == SNACKBAR_UNDO_REMOVE) {
            lastRemoved?.let { setFavourite(it, favourite = true) }
            lastRemoved = null
            return
        }
        super.onSystemEvent(event)
    }

    // loading = {}: the list is the screen's content, and an overlay over an empty Home on first
    // frame is worse than the empty section it replaces.
    private fun observeFavourites() = observe(
        flow = { favouritesRepository.observeFavourites() },
        loading = {},
        errorDisplay = ErrorDisplay.Silent,
        onData = { favourites ->
            uiState.update { state ->
                state.copy(data = state.data?.copy(favourites = favourites))
            }
        },
    )

    private fun removeFavourite(productId: String) {
        lastRemoved = productId
        setFavourite(productId, favourite = false)
        showSnackbar(
            message = R.string.home_favourite_removed.toUiText(),
            actionLabel = R.string.home_favourite_undo.toUiText(),
            id = SNACKBAR_UNDO_REMOVE,
        )
    }

    // No onData work: the observed flow is what updates the list, so writing it here too would be
    // a second source of truth for the same row.
    private fun setFavourite(productId: String, favourite: Boolean) = execute(
        loading = {},
        action = { favouritesRepository.setFavourite(productId, favourite) },
        onData = { },
    )
}
