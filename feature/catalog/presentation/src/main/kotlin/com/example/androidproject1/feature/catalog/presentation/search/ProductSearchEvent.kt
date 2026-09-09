package com.example.androidproject1.feature.catalog.presentation.search

import com.example.androidproject1.core.ui.event.UiEvent
import com.example.androidproject1.feature.catalog.domain.Product

sealed interface ProductSearchEvent : UiEvent {

    /** Every keystroke. The debounce is downstream of this, not in the field. */
    data class QueryChanged(val query: String) : ProductSearchEvent

    /** A previous search, tapped. Fills the field and searches again. */
    data class RecentClicked(val query: String) : ProductSearchEvent

    data object ClearRecentsClicked : ProductSearchEvent

    data class ProductClicked(val product: Product) : ProductSearchEvent

    data object NavigateUpClicked : ProductSearchEvent
}
