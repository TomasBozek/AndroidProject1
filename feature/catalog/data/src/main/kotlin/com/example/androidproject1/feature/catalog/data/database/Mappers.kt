package com.example.androidproject1.feature.catalog.data.database

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

internal fun CategoryEntity.toDomain() = Category(id = id, name = name)

internal fun ProductEntity.toDomain() = Product(
    id = id,
    categoryId = categoryId,
    name = name,
    price = price,
    description = description,
)

internal fun Category.toEntity() = CategoryEntity(id = id, name = name)

internal fun Product.toEntity() = ProductEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    price = price,
    description = description,
)
