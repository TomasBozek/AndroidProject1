package com.example.androidproject1.feature.catalog.domain

import com.example.androidproject1.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Implemented in the data layer. Separate from [CatalogRepository] because the catalog is what the
 * shop sells and a favourite is what this user chose — `feat.5` replaces the first wholesale on
 * every refresh and must not touch the second.
 */
interface FavouritesRepository {

    fun observeFavourites(): Flow<Outcome<List<Product>>>

    fun observeIsFavourite(productId: String): Flow<Outcome<Boolean>>

    suspend fun setFavourite(productId: String, favourite: Boolean): Outcome<Unit>
}
