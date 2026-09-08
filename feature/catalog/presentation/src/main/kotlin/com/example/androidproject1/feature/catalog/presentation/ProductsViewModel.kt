package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import kotlinx.coroutines.flow.update

class ProductsViewModel(
    logger: Logger,
    // The route key, handed in by the destination. Available in `init`, and it comes back with
    // the back stack entry after process death.
    private val args: ProductsDestination,
    private val catalogRepository: CatalogRepository,
) : BaseViewModel<ProductsState, ProductsEvent, ProductsNavigation>(
    // Nothing to show until the category named by the route has loaded its products.
    initialState = null,
    logger = logger.withTag("ProductsViewModel"),
) {

    init {
        load()
    }

    override fun onUiEvent(event: ProductsEvent) {
        when (event) {
            is ProductsEvent.ProductClicked -> navigate(ProductsNavigation.ProductDetail(event.product.id))
        }
    }

    // Inline rather than Alert: this is the call that loads the screen, so a dialog would leave
    // nothing behind it. BaseViewModel remembers the call and the retry button re-runs it.
    private fun load() = execute(
        errorDisplay = ErrorDisplay.Inline,
        action = { catalogRepository.getProducts(args.categoryId) },
        onData = { products ->
            if (products.isEmpty()) {
                showContent(
                    ContentState.Empty(message = R.string.products_empty.toUiText()),
                )
                return@execute
            }
            uiState.update {
                it.copy(data = ProductsState(categoryName = args.categoryName, products = products))
            }
        },
    )
}
