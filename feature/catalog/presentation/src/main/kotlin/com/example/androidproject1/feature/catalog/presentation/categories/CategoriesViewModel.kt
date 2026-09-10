package com.example.androidproject1.feature.catalog.presentation.categories

import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.presentation.R
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.ui.text.toUiText
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
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
            CategoriesEvent.SearchClicked -> navigate(CategoriesNavigation.Search)

            is CategoriesEvent.CategoryClicked -> navigate(
                CategoriesNavigation.Products(categoryId = event.category.id, categoryName = event.category.name),
            )
        }
    }

    /**
     * Cache first, then the network — so a cold start with no connection still shows what was
     * browsed last time, and `observe` keeps the screen current if the cache changes underneath.
     *
     * Inline rather than Alert: a dialog over the loading screen would leave nothing behind it.
     * The refresh failure that follows a stale hit is claimed by [keepStaleContent] instead, so a
     * usable list is not replaced by an error page.
     */
    private fun loadCategories() = observe(
        flow = { catalogRepository.observeCategories() },
        errorDisplay = ErrorDisplay.Inline,
        onError = ::keepStaleContent,
        onData = { categories -> uiState.update { it.copy(data = CategoriesState(categories = categories)) } },
    )

    /**
     * Keeps a stale list on screen when the refresh behind it fails.
     *
     * `cached` emits the cache and then the failure, and `ErrorDisplay.Inline` renders a
     * `ContentState` *instead of* the content — so without this a screen that had something usable
     * to show would be replaced by "try again". Returning true claims the error; the snackbar is
     * what stops the staleness being silent.
     */
    private fun keepStaleContent(error: DomainError): Boolean {
        val hasContent = uiState.value.data != null
        if (hasContent) showSnackbar(R.string.catalog_stale.toUiText())
        return hasContent
    }
}
