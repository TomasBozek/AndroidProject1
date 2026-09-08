package com.example.androidproject1.feature.catalog.data

import com.example.androidproject1.feature.catalog.domain.Category
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.catalog.gateway.LocalCatalogDataSource

/**
 * Mock catalog, held in memory so the app has something to browse. Swap for a network client or a
 * DAO and keep the interface where it is.
 */
class DefaultLocalCatalogDataSource : LocalCatalogDataSource {

    override suspend fun getCategories(): List<Category> = CATEGORIES

    override suspend fun getProducts(categoryId: String): List<Product> =
        PRODUCTS.filter { it.categoryId == categoryId }

    override suspend fun getProduct(productId: String): Product? =
        PRODUCTS.find { it.id == productId }

    private companion object {

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
}
