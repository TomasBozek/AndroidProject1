package com.example.androidproject1.feature.cart.domain.test

import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.domain.CartRepository
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [CartRepository], beside the interface it fakes. */
class FakeCartRepository(
    initial: List<CartItem> = emptyList(),
    var failWith: DomainError? = null,
) : CartRepository {

    private val items = MutableStateFlow(initial)

    val current: List<CartItem> get() = items.value

    override fun observeItems(): Flow<Outcome<List<CartItem>>> =
        items.map { failWith?.let { error -> Outcome.Failure(error) } ?: Outcome.Success(it) }

    override fun observeCount(): Flow<Outcome<Int>> =
        items.map { list ->
            failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(list.sumOf { it.quantity })
        }

    override suspend fun add(item: CartItem): Outcome<Unit> = write {
        val existing = items.value.find { it.productId == item.productId }
        items.value = if (existing == null) {
            items.value + item
        } else {
            // Quantities add, and the row keeps its place — the same contract the DAO has.
            items.value.map {
                if (it.productId == item.productId) it.copy(quantity = it.quantity + item.quantity) else it
            }
        }
    }

    override suspend fun setQuantity(productId: String, quantity: Int): Outcome<Unit> = write {
        items.value = if (quantity <= 0) {
            items.value.filterNot { it.productId == productId }
        } else {
            items.value.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        }
    }

    override suspend fun remove(productId: String): Outcome<Unit> = write {
        items.value = items.value.filterNot { it.productId == productId }
    }

    override suspend fun clear(): Outcome<Unit> = write { items.value = emptyList() }

    private inline fun write(block: () -> Unit): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        block()
        return Outcome.Success(Unit)
    }
}
