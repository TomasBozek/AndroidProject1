package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.Flow

/**
 * Internal to the data layer, like [LocalCatalogDataSource]: what the rest of the app depends on
 * is `FavouritesRepository` in `domain`.
 */
interface LocalFavouritesDataSource {

    fun observeFavourites(): Flow<List<Product>>

    fun observeIsFavourite(productId: String): Flow<Boolean>

    suspend fun setFavourite(productId: String, favourite: Boolean)
}
