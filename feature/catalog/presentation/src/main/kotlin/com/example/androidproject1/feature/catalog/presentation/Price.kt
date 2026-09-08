package com.example.androidproject1.feature.catalog.presentation

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a price held in minor units for [locale]'s currency: 450 becomes `$4.50`, `4,50 €` or
 * whatever that locale writes.
 *
 * Shared by ProductsScreen and ProductDetailScreen. The scale comes from the currency rather than a
 * hardcoded 2, because not every currency has two decimal places.
 */
fun Long.asPrice(locale: Locale = Locale.getDefault()): String {
    val format = NumberFormat.getCurrencyInstance(locale)
    val fractionDigits = format.currency?.defaultFractionDigits?.coerceAtLeast(0) ?: 2
    return format.format(BigDecimal.valueOf(this, fractionDigits))
}
