package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.domain.Logger
import com.example.androidproject1.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.core.ui.viewmodel.ErrorDisplay
import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import kotlinx.coroutines.flow.update

class CategoriesViewModel(
    logger: Logger,
    private val catalogRepository: CatalogRepository,
) : BaseViewModel<CategoriesState, CategoriesEvent, CategoriesNavigation>(
    // Nothing to show until the catalog has loaded.
    initialState = null,
    logger = logger.withTag("CategoriesViewModel"),
) {

    init {
        loadCategories()
    }

    override fun onUiEvent(event: CategoriesEvent) {
        when (event) {
            is CategoriesEvent.CategoryClicked -> navigate(
                CategoriesNavigation.Products(categoryId = event.category.id, categoryName = event.category.name),
            )
        }
    }

    // Inline rather than Alert: this is the call that loads the screen. See ProductsViewModel.
    private fun loadCategories() = execute(
        errorDisplay = ErrorDisplay.Inline,
        action = { catalogRepository.getCategories() },
        onData = { categories -> uiState.update { it.copy(data = CategoriesState(categories = categories)) } },
    )
}
