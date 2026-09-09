package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM categories")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    /**
     * `LIKE` is case-insensitive for ASCII in SQLite, which is what a search box wants; the
     * wildcards are concatenated rather than written into the parameter so a query containing
     * `%` searches for a per cent sign instead of matching everything.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun observeProductsMatching(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun observeProductsIn(categoryId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    fun observeProduct(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun product(productId: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun productCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteCategories()

    @Query("DELETE FROM products WHERE categoryId = :categoryId")
    suspend fun deleteProductsIn(categoryId: String)

    /**
     * Replace, in one transaction.
     *
     * Delete-then-insert rather than upsert, because a product the server has dropped has to
     * disappear — an upsert would leave it in the table for ever. The transaction is what stops a
     * collector seeing the empty moment in between.
     */
    @Transaction
    suspend fun replaceCategories(categories: List<CategoryEntity>) {
        deleteCategories()
        insertCategories(categories)
    }

    @Transaction
    suspend fun replaceProductsIn(categoryId: String, products: List<ProductEntity>) {
        deleteProductsIn(categoryId)
        insertProducts(products)
    }
}
