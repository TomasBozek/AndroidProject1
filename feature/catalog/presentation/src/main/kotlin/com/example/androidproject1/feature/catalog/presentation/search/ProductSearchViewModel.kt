package com.example.androidproject1.feature.catalog.presentation.search

import com.example.androidproject1.feature.catalog.domain.CatalogRepository
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.catalog.domain.RecentSearchesRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.ui.viewmodel.BaseViewModel
import com.example.androidproject1.service.core.ui.viewmodel.ErrorDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import com.example.androidproject1.service.core.domain.result.map as mapOutcome

/**
 * Two loads on one screen, each under its own content id.
 *
 * That is the whole point of the id: `ErrorDisplay.Inline` remembers the failed call per id, so the
 * retry re-runs the one that failed rather than whichever call was last. Results failing leaves
 * recents alone, and the other way round.
 */
class ProductSearchViewModel(
    logger: Logger,
    private val catalogRepository: CatalogRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
) : BaseViewModel<ProductSearchState, ProductSearchEvent, ProductSearchNavigation>(
    initialState = ProductSearchState.EMPTY,
    logger = logger.withTag("ProductSearchViewModel"),
) {

    // The field's own value, and the source the search is debounced from. Held here rather than
    // read back out of the state so the debounce sees keystrokes and not every state change.
    private val query = MutableStateFlow("")

    init {
        observeResults()

        observe(
            flow = { recentSearchesRepository.observeRecents() },
            errorDisplay = ErrorDisplay.Inline,
            alertId = CONTENT_ID_RECENTS,
        ) { recents ->
            updateData { copy(recents = recents) }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeResults() = observe(
        flow = {
            query
                // Typing "coffee" is six keystrokes and would be six queries; 300 ms is long
                // enough to be one and short enough that nobody waits for it.
                .debounce(DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                // flatMapLatest, so a query still running when the next one arrives is cancelled
                // rather than racing it to the state.
                .flatMapLatest { text ->
                    if (text.isBlank()) {
                        flowOf(Outcome.Success(Results(query = text, products = emptyList())))
                    } else {
                        catalogRepository.searchProducts(text)
                            .map { outcome -> outcome.mapOutcome { Results(text, it) } }
                    }
                }
        },
        errorDisplay = ErrorDisplay.Inline,
        alertId = CONTENT_ID_RESULTS,
    ) { results ->
        updateData {
            copy(results = results.products, searched = results.query.isNotBlank())
        }
        // Recorded on the result rather than on the keystroke: what reaches the list is what the
        // user actually searched for, not every prefix they typed on the way there.
        if (results.query.isNotBlank()) recentSearchesRepository.record(results.query)
    }

    override fun onUiEvent(event: ProductSearchEvent) {
        when (event) {
            // The field is echoed immediately and the search follows 300 ms later: a field that
            // waited for the debounce would drop characters under a fast typist.
            is ProductSearchEvent.QueryChanged -> {
                updateData { copy(query = event.query) }
                query.value = event.query
            }

            is ProductSearchEvent.RecentClicked -> {
                updateData { copy(query = event.query) }
                query.value = event.query
            }

            ProductSearchEvent.ClearRecentsClicked -> execute(
                action = { recentSearchesRepository.clear() },
                onData = {},
            )

            is ProductSearchEvent.ProductClicked ->
                navigate(ProductSearchNavigation.ProductDetail(event.product.id))

            ProductSearchEvent.NavigateUpClicked -> navigate(ProductSearchNavigation.NavigateUp)
        }
    }

    /** The query a result set belongs to, carried with it so a stale emission cannot be mistaken. */
    private data class Results(val query: String, val products: List<Product>)

    private companion object {

        const val DEBOUNCE_MILLIS = 300L

        const val CONTENT_ID_RESULTS = "results"

        const val CONTENT_ID_RECENTS = "recents"
    }
}
