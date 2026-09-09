package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.ui.event.UiEvent

sealed interface ProductPickerEvent : UiEvent {

    data class ProductClicked(val product: Product) : ProductPickerEvent
}

private typealias Product = com.example.androidproject1.feature.catalog.domain.Product
