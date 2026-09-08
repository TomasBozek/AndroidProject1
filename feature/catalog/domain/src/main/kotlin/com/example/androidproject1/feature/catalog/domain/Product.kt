package com.example.androidproject1.feature.catalog.domain

/**
 * @property price in minor units — 450 is 4.50. Integer money cannot drift the way a `Double` can,
 * and it leaves the currency and its formatting to the layer that knows the user's locale.
 */
data class Product(
    val id: String,
    val categoryId: String,
    val name: String,
    val price: Long,
    val description: String,
)
