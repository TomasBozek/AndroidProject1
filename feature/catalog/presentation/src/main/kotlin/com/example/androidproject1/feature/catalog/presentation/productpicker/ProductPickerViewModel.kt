package com.example.androidproject1.feature.catalog.presentation.productpicker

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import kotlinx.coroutines.flow.update

class ProductPickerViewModel(
    logger: Logger,
    private val args: ProductPickerDestination,
    private val catalogRepository: CatalogRepository,
) : BaseViewModel<ProductPickerState, ProductPickerEvent, ProductPickerNavigation>(
    initialState = ProductPickerState(),
    logger = logger.withTag("ProductPickerViewModel"),
) {

    /** The requester\'s key, carried in by the route so it survives process death with the entry. */
    val resultKey: String get() = args.resultKey

    init {
        observeProducts()
    }

    override fun onUiEvent(event: ProductPickerEvent) = when (event) {
        is ProductPickerEvent.ProductClicked ->
            navigate(ProductPickerNavigation.Picked(event.product.id))
    }

    private fun observeProducts() = observe(
        flow = { catalogRepository.observeAllProducts() },
        loading = {},
        errorDisplay = ErrorDisplay.Inline,
        onData = { products -> uiState.update { it.copy(data = ProductPickerState(products)) } },
    )
}
