package com.example.androidproject1.feature.cart.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * A line in the cart.
 *
 * The product is denormalised into the row rather than joined from the catalog, and deliberately:
 * `feat.5` replaces the catalog table on every refresh, so a joined cart would empty itself when
 * the shop reorganised. What the user put in the basket is theirs, at the price they saw.
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Long,
    val quantity: Int,
    val addedAt: Long,
)

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    fun observeItems(): Flow<List<CartItemEntity>>

    /** Drives the tab badge, so it is a count and not a list of rows nobody draws. */
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun item(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun remove(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}

@Database(entities = [CartItemEntity::class], version = 1, exportSchema = true)
abstract class CartDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao

    companion object {

        const val NAME = "cart.db"
    }
}
