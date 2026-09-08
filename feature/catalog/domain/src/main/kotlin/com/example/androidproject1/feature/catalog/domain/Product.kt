package com.example.androidproject1.feature.catalog.domain

data class Product(
    val id: String,
    val categoryId: String,
    val name: String,
    val price: Double,
    val description: String,
)
