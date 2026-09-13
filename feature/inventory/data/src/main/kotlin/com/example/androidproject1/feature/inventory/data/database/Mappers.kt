package com.example.androidproject1.feature.inventory.data.database

import com.example.androidproject1.feature.inventory.domain.Item

internal fun ItemEntity.toDomain() = Item(
    id = id,
    name = name,
    category = category,
    condition = condition,
    quantity = quantity,
    priceMinor = priceMinor,
    acquiredOn = acquiredOn,
    insured = insured,
    tags = tags,
    owner = owner,
    imageUrl = imageUrl,
    notes = notes,
)

internal fun Item.toEntity() = ItemEntity(
    id = id,
    name = name,
    category = category,
    condition = condition,
    quantity = quantity,
    priceMinor = priceMinor,
    acquiredOn = acquiredOn,
    insured = insured,
    tags = tags,
    owner = owner,
    imageUrl = imageUrl,
    notes = notes,
)
