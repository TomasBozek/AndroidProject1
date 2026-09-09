package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product

/**
 * The mock catalog, now the database's seed rather than the data source's backing list.
 *
 * `feat.5` replaces this with a network fetch writing into the same tables; nothing above the data
 * layer changes when it does, because everything already reads the database.
 */
internal object CatalogSeed {

    val CATEGORIES = listOf(
        Category(id = "beverages", name = "Beverages"),
        Category(id = "bakery", name = "Bakery"),
        Category(id = "produce", name = "Produce"),
    )

    val PRODUCTS = listOf(
        Product(
            id = "coffee",
            categoryId = "beverages",
            name = "Coffee",
            price = 450,
            description = "Freshly ground, brewed to order.",
        ),
        Product(
            id = "tea",
            categoryId = "beverages",
            name = "Tea",
            price = 300,
            description = "A pot of loose-leaf tea.",
        ),
        Product(
            id = "orange-juice",
            categoryId = "beverages",
            name = "Orange juice",
            price = 350,
            description = "Cold-pressed, no added sugar.",
        ),
        Product(
            id = "croissant",
            categoryId = "bakery",
            name = "Croissant",
            price = 275,
            description = "Buttery, baked fresh every morning.",
        ),
        Product(
            id = "baguette",
            categoryId = "bakery",
            name = "Baguette",
            price = 325,
            description = "A crisp, classic French loaf.",
        ),
        Product(
            id = "apple",
            categoryId = "produce",
            name = "Apple",
            price = 75,
            description = "Crisp and locally grown.",
        ),
        Product(
            id = "banana",
            categoryId = "produce",
            name = "Banana",
            price = 50,
            description = "Ripe and ready to eat.",
        ),
    )
}
