package com.example.androidproject1.feature.catalog.presentation.productpicker

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.catalog.domain.Product

@Immutable
data class ProductPickerState(
    val products: List<Product> = emptyList(),
) {

    companion object {

        val PREVIEW = ProductPickerState(
            products = listOf(
                Product("coffee", "beverages", "Coffee", 450, "Freshly ground."),
                Product("croissant", "bakery", "Croissant", 275, "Baked this morning."),
            ),
        )
    }
}
