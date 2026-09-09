package com.example.androidproject1.feature.catalog.data.source

import com.example.androidproject1.feature.catalog.data.database.FavouriteEntity
import com.example.androidproject1.feature.catalog.data.database.FavouritesDao
import com.example.androidproject1.feature.catalog.data.database.toDomain
import com.example.androidproject1.feature.catalog.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultLocalFavouritesDataSource(
    private val favouritesDao: FavouritesDao,
    private val now: () -> Long = System::currentTimeMillis,
) : LocalFavouritesDataSource {

    override fun observeFavourites(): Flow<List<Product>> =
        favouritesDao.observeFavourites().map { rows -> rows.map { it.toDomain() } }

    override fun observeIsFavourite(productId: String): Flow<Boolean> =
        favouritesDao.observeIsFavourite(productId)

    override suspend fun setFavourite(productId: String, favourite: Boolean) {
        if (favourite) {
            // The timestamp is what orders the Favourites section, so re-favouriting moves a
            // product back to the front rather than leaving it where it was.
            favouritesDao.add(FavouriteEntity(productId = productId, favouritedAt = now()))
        } else {
            favouritesDao.remove(productId)
        }
    }
}
