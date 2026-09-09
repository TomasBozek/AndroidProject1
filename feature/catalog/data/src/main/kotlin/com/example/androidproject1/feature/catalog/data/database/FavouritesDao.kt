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
     *
     * The second sort key makes that order total: `favouritedAt` is a millisecond, so two products
     * favourited inside one are tied, and a tie is ordered by whatever SQLite feels like — which
     * for an [OnConflictStrategy.REPLACE] upsert is the row's *new* rowid, so re-favouriting one
     * of them could reshuffle both.
     */
    @Query(
        """
        SELECT products.* FROM products
        INNER JOIN favourites ON favourites.productId = products.id
        ORDER BY favourites.favouritedAt DESC, products.id ASC
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
