package com.example.androidproject1.feature.catalog.presentation.productdetail

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.cart.domain.AddProductToCart
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.catalog.presentation.R
import kotlinx.coroutines.flow.update

class ProductDetailViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: ProductDetailDestination,
    private val catalogRepository: CatalogRepository,
    private val favouritesRepository: FavouritesRepository,
    private val addProductToCart: AddProductToCart,
) : BaseViewModel<ProductDetailState, ProductDetailEvent, ProductDetailNavigation>(
    // Nothing to show until the product named by the route has loaded.
    initialState = null,
    logger = logger.withTag("ProductDetailViewModel"),
) {

    companion object {

        /** Its own id, so the "go back" action is told apart from a retry of a failed load. */
        const val CONTENT_NOT_FOUND = "product_not_found"
    }

    // The screen is two independent reads — the product, once, and the favourite flag, for as long
    // as the screen lives — and either can arrive first. Each used to write the whole state: the
    // load overwrote a flag that had already arrived, and the flag was dropped outright while
    // `data` was still null. Both now write their own half here and the state is composed from the
    // pair, so whichever loses the race is still in the answer.
    private var product: Product? = null
    private var isFavourite = false

    init {
        load()
        observeFavourite()
    }

    private fun publish() {
        val product = product ?: return
        uiState.update {
            it.copy(data = ProductDetailState(product = product, isFavourite = isFavourite))
        }
    }

    override fun onUiEvent(event: ProductDetailEvent) = when (event) {
        ProductDetailEvent.FavouriteToggled -> toggleFavourite()

        ProductDetailEvent.AddToCartClicked -> addToCart()

        ProductDetailEvent.NavigateUpClicked -> navigate(ProductDetailNavigation.NavigateUp)
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
        onData = { favourite ->
            isFavourite = favourite
            publish()
        },
    )

    /**
     * The write runs here, in the ViewModel's own scope.
     *
     * It used to be launched from `AppNavHost`'s `rememberCoroutineScope`, which a rotation just
     * after the tap cancels — the item never reached the cart and nothing said so. A ViewModel
     * survives a configuration change, so this one does not. The product is already loaded, so
     * there is no second read of it either.
     */
    private fun addToCart() {
        val product = product ?: return
        execute(
            loading = {},
            action = { addProductToCart(productId = product.id, name = product.name, price = product.price) },
            onData = { showSnackbar(R.string.product_detail_added_to_cart.toUiText()) },
        )
    }

    // Alert on failure: the user asked for this, so silence would look like the tap did nothing.
    private fun toggleFavourite() {
        execute(
            loading = {},
            action = { favouritesRepository.setFavourite(args.productId, !isFavourite) },
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
            this.product = product
            publish()
        },
    )
}
