package com.example.androidproject1.feature.catalog.data.repository

import com.example.androidproject1.feature.catalog.data.source.LocalFavouritesDataSource
import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultFavouritesRepository(
    logger: Logger,
    private val localFavouritesDataSource: LocalFavouritesDataSource,
) : FavouritesRepository, BaseRepository(logger = logger.withTag("DefaultFavouritesRepository")) {

    // retries, because these collectors outlive a failure: Home holds its flow for as long as the
    // tab exists, so one transient database error must not stop it emitting for good.
    override fun observeFavourites(): Flow<Outcome<List<Product>>> =
        observe(source = localFavouritesDataSource.observeFavourites(), retries = RETRIES)

    override fun observeIsFavourite(productId: String): Flow<Outcome<Boolean>> =
        observe(source = localFavouritesDataSource.observeIsFavourite(productId), retries = RETRIES)

    override suspend fun setFavourite(productId: String, favourite: Boolean): Outcome<Unit> =
        execute { localFavouritesDataSource.setFavourite(productId, favourite) }

    private companion object {

        const val RETRIES = 3L
    }
}
