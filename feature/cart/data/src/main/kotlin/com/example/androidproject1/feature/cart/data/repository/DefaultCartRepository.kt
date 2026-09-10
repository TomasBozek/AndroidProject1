package com.example.androidproject1.feature.cart.data.repository

import com.example.androidproject1.feature.cart.data.source.LocalCartDataSource
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.domain.CartRepository
import com.example.androidproject1.service.core.data.BaseRepository
import com.example.androidproject1.service.core.domain.Logger
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

class DefaultCartRepository(
    logger: Logger,
    private val localCartDataSource: LocalCartDataSource,
) : CartRepository, BaseRepository(logger = logger.withTag("DefaultCartRepository")) {

    // retries: the badge is collected for as long as the app is in the main flow, so one
    // transient read failure would otherwise stop it updating for the rest of the session.
    override fun observeItems(): Flow<Outcome<List<CartItem>>> =
        observe(source = localCartDataSource.observeItems(), retries = RETRIES)

    override fun observeCount(): Flow<Outcome<Int>> =
        observe(source = localCartDataSource.observeCount(), retries = RETRIES)

    override suspend fun add(item: CartItem): Outcome<Unit> = execute {
        localCartDataSource.add(item)
    }

    override suspend fun setQuantity(productId: String, quantity: Int): Outcome<Unit> = execute {
        localCartDataSource.setQuantity(productId, quantity)
    }

    override suspend fun remove(productId: String): Outcome<Unit> = execute {
        localCartDataSource.remove(productId)
    }

    override suspend fun clear(): Outcome<Unit> = execute { localCartDataSource.clear() }

    private companion object {

        const val RETRIES = 3L
    }
}
