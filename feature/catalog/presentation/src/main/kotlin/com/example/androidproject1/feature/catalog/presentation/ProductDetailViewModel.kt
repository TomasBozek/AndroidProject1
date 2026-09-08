package com.example.androidproject1.feature.catalog.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import kotlinx.coroutines.flow.update

class ProductDetailViewModel(
    logger: Logger,
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
) : BaseViewModel<ProductDetailState, ProductDetailEvent, ProductDetailNavigation>(
    // Nothing to show until the product named by the route has loaded.
    initialState = null,
    logger = logger.withTag("ProductDetailViewModel"),
    savedStateHandle = savedStateHandle,
) {

    companion object {

        /** Its own id, so the "go back" action is told apart from a retry of a failed load. */
        const val CONTENT_NOT_FOUND = "product_not_found"
    }

    // Decoded from the route, so it is available here in init and restored after process death.
    private val args = navArgs<ProductDetailDestination>()

    init {
        load()
    }

    override fun onSystemEvent(event: SystemEvent) {
        if (event is SystemEvent.ContentAction && event.id == CONTENT_NOT_FOUND) {
            // There is no product to come back to, so the only useful action is leaving.
            sendCommand(UiCommand.NavigateBack)
            return
        }
        super.onSystemEvent(event)
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
