package com.example.androidproject1.feature.catalog.presentation

/** Shared by ProductsScreen and ProductDetailScreen. */
fun Double.asPrice(): String = "$%.2f".format(this)
