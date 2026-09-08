package com.example.androidproject1.feature.catalog.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
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

    // Decoded from the route, so it is available here in init and restored after process death.
    private val args = navArgs<ProductDetailDestination>()

    init {
        load()
    }

    private fun load() = execute(
        action = { catalogRepository.getProduct(args.productId) },
        onData = { product -> uiState.update { it.copy(data = ProductDetailState(product = product)) } },
    )
}
