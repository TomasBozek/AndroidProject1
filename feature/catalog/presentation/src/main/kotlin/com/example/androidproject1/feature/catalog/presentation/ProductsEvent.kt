package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.ui.event.UiEvent
import com.example.androidproject1.feature.catalog.domain.Product

sealed interface ProductsEvent : UiEvent {

    data class ProductClicked(val product: Product) : ProductsEvent
}
