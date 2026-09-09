package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CatalogDao {

    @Query("SELECT * FROM categories")
    suspend fun categories(): List<CategoryEntity>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    suspend fun productsIn(categoryId: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun product(productId: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun productCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
}
