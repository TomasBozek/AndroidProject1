package com.example.androidproject1.feature.catalog.presentation.productdetail

import androidx.compose.runtime.Immutable
import com.example.androidproject1.feature.catalog.domain.Product

@Immutable
data class ProductDetailState(
    val product: Product,
    val isFavourite: Boolean = false,
) {

    companion object {

        val PREVIEW = ProductDetailState(
            product = Product(
                id = "coffee",
                categoryId = "beverages",
                name = "Coffee",
                price = 450,
                description = "Freshly ground, brewed to order.",
            ),
        )
    }
}
