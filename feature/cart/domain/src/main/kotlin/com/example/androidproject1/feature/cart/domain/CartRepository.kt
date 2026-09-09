package com.example.androidproject1.feature.cart.domain

import com.example.androidproject1.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/** Implemented in the data layer. Declared here so the domain layer depends on nothing. */
interface CartRepository {

    fun observeItems(): Flow<Outcome<List<CartItem>>>

    /** Total quantity, for the tab badge. Separate from [observeItems] so the badge costs one sum. */
    fun observeCount(): Flow<Outcome<Int>>

    suspend fun add(item: CartItem): Outcome<Unit>

    suspend fun setQuantity(productId: String, quantity: Int): Outcome<Unit>

    suspend fun remove(productId: String): Outcome<Unit>

    suspend fun clear(): Outcome<Unit>
}
