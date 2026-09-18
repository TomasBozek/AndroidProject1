package com.example.androidproject1.feature.catalog.presentation.component

import com.example.androidproject1.feature.catalog.domain.Product

/**
 * The keys the product row and the product detail share (F4S1): one string per element, built
 * from the id, so a row and the screen it opens name the same thing and no other row does.
 * Not composables, so not a component — but here rather than in a screen file, because a
 * screen file holds the screen and its previews and nothing else.
 */
fun Product.sharedNameKey(): String = "product/$id/name"

fun Product.sharedPriceKey(): String = "product/$id/price"
