package com.example.androidproject1.feature.catalog.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {

    /**
     * The join is why products live in the database at all: one query answers "which products are
     * favourited", in the order they were added, without loading the catalog to filter it.
     */
    @Query(
        """
        SELECT products.* FROM products
        INNER JOIN favourites ON favourites.productId = products.id
        ORDER BY favourites.favouritedAt DESC
        """,
    )
    fun observeFavourites(): Flow<List<ProductEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE productId = :productId)")
    fun observeIsFavourite(productId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE productId = :productId")
    suspend fun remove(productId: String)
}
