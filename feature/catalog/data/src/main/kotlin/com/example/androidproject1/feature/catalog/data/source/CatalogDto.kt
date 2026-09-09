package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.serialization.Serializable

/**
 * The wire shape, deliberately not the domain's.
 *
 * `Product` is what the app reasons about; these are what the server happens to send today. Keeping
 * them apart is what stops a renamed JSON field reaching a ViewModel.
 */
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
)

@Serializable
data class ProductDto(
    val id: String,
    val categoryId: String,
    val name: String,
    /** Minor units, as the domain holds it — 450 is 4.50. */
    val price: Long,
    val description: String,
)

internal fun CategoryDto.toDomain() = Category(id = id, name = name)

internal fun ProductDto.toDomain() = Product(
    id = id,
    categoryId = categoryId,
    name = name,
    price = price,
    description = description,
)
