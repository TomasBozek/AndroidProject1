package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import kotlinx.coroutines.flow.update

class ProductDetailViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: ProductDetailDestination,
    private val catalogRepository: CatalogRepository,
    private val favouritesRepository: FavouritesRepository,
) : BaseViewModel<ProductDetailState, ProductDetailEvent, ProductDetailNavigation>(
    // Nothing to show until the product named by the route has loaded.
    initialState = null,
    logger = logger.withTag("ProductDetailViewModel"),
) {

    companion object {

        /** Its own id, so the "go back" action is told apart from a retry of a failed load. */
        const val CONTENT_NOT_FOUND = "product_not_found"
    }

    init {
        load()
        observeFavourite()
    }

    override fun onUiEvent(event: ProductDetailEvent) = when (event) {
        ProductDetailEvent.FavouriteToggled -> toggleFavourite()
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.ContentAction && event.id == CONTENT_NOT_FOUND) {
            // There is no product to come back to, so the only useful action is leaving.
            sendCommand(UiCommand.NavigateBack)
            return
        }
        super.onSystemEvent(event)
    }

    /**
     * The heart follows the database, not the tap.
     *
     * Writing the state optimistically and letting the flow confirm it would mean two sources of
     * truth for one boolean; observing means a favourite removed from Home is already un-hearted
     * when the user navigates back here. `loading = {}` because a heart is not worth an overlay.
     */
    private fun observeFavourite() = observe(
        flow = { favouritesRepository.observeIsFavourite(args.productId) },
        loading = {},
        errorDisplay = ErrorDisplay.Silent,
        onData = { isFavourite ->
            uiState.update { state ->
                state.copy(data = state.data?.copy(isFavourite = isFavourite))
            }
        },
    )

    // Alert on failure: the user asked for this, so silence would look like the tap did nothing.
    private fun toggleFavourite() {
        val current = uiState.value.data?.isFavourite ?: return
        execute(
            loading = {},
            action = { favouritesRepository.setFavourite(args.productId, !current) },
            onData = { },
        )
    }

    // Inline rather than Alert: this is the call that loads the screen, so a dialog would leave
    // nothing behind it. BaseViewModel remembers the call and the retry button re-runs it.
    private fun load() = execute(
        errorDisplay = ErrorDisplay.Inline,
        action = { catalogRepository.getProduct(args.productId) },
        onData = { product ->
            if (product == null) {
                // A success that found nothing, not a failure — the same distinction ProductsScreen
                // draws for an empty category.
                showContent(
                    ContentState.Empty(
                        id = CONTENT_NOT_FOUND,
                        message = R.string.product_detail_not_found.toUiText(),
                        actionLabel = R.string.product_detail_go_back.toUiText(),
                    ),
                )
                return@execute
            }
            uiState.update { it.copy(data = ProductDetailState(product = product)) }
        },
    )
}
