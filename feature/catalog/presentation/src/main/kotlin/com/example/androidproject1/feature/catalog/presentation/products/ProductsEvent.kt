package com.example.androidproject1.feature.catalog.presentation.products

import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.service.core.ui.event.UiEvent

sealed interface ProductsEvent : UiEvent {

    data class ProductClicked(val product: Product) : ProductsEvent

    /** The Up arrow was tapped. Every non-root screen has one. */
    data object NavigateUpClicked : ProductsEvent
}
