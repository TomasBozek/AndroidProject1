package com.example.androidproject1.feature.catalog.presentation.products

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.presentation.R
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
            ProductsEvent.NavigateUpClicked -> navigate(ProductsNavigation.NavigateUp)
        }
    }

    // Cache then network, so a category browsed before opens instantly and offline. Inline
    // rather than Alert: `cached` emits a refresh failure after the stale list, so the error
    // lands beside a usable screen instead of replacing it.
    private fun load() = observe(
        flow = { catalogRepository.observeProducts(args.categoryId) },
        errorDisplay = ErrorDisplay.Inline,
        onError = ::keepStaleContent,
        onData = { products ->
            if (products.isEmpty()) {
                showContent(
                    ContentState.Empty(message = R.string.products_empty.toUiText()),
                )
                return@observe
            }
            // Clear a previous empty state: the list can fill in on the refresh that follows.
            clearContent()
            uiState.update {
                it.copy(data = ProductsState(categoryName = args.categoryName, products = products))
            }
        },
    )

    /** See `CategoriesViewModel.keepStaleContent`: a usable list beats an error page. */
    private fun keepStaleContent(error: DomainError): Boolean {
        val hasContent = uiState.value.data != null
        if (hasContent) showSnackbar(R.string.catalog_stale.toUiText())
        return hasContent
    }
}
