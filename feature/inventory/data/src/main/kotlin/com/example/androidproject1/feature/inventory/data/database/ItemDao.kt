package com.example.androidproject1.feature.inventory.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT COUNT(*) FROM items")
    suspend fun count(): Int

    @Query("SELECT * FROM items ORDER BY name ASC")
    fun observeItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    fun observeItem(itemId: String): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItem(itemId: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ItemEntity)

    /** The seed: `IGNORE`, so a row the user has since edited is never overwritten. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items WHERE id IN (:itemIds)")
    suspend fun delete(itemIds: Set<String>)
}
