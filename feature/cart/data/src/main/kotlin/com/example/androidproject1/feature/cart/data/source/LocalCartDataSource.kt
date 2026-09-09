package com.example.androidproject1.feature.cart.data.source

import com.example.androidproject1.feature.cart.domain.CartItem
import kotlinx.coroutines.flow.Flow

/** Internal to the data layer: what the rest of the app depends on is `CartRepository`. */
interface LocalCartDataSource {

    fun observeItems(): Flow<List<CartItem>>

    fun observeCount(): Flow<Int>

    suspend fun add(item: CartItem)

    suspend fun setQuantity(productId: String, quantity: Int)

    suspend fun remove(productId: String)

    suspend fun clear()
}
