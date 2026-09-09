package com.example.androidproject1.feature.cart.data.source

import com.example.androidproject1.feature.cart.data.database.CartDao
import com.example.androidproject1.feature.cart.data.database.CartItemEntity
import com.example.androidproject1.feature.cart.domain.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultLocalCartDataSource(
    private val cartDao: CartDao,
    private val now: () -> Long = System::currentTimeMillis,
) : LocalCartDataSource {

    override fun observeItems(): Flow<List<CartItem>> =
        cartDao.observeItems().map { rows -> rows.map { it.toDomain() } }

    override fun observeCount(): Flow<Int> = cartDao.observeCount()

    /**
     * Adding something already in the cart increases its quantity.
     *
     * The original `addedAt` is kept, so a second tap does not reorder the list under the user\'s
     * finger — which is the kind of thing that makes a list feel unstable for no visible reason.
     */
    override suspend fun add(item: CartItem) {
        val existing = cartDao.item(item.productId)
        cartDao.upsert(
            CartItemEntity(
                productId = item.productId,
                name = item.name,
                price = item.price,
                quantity = (existing?.quantity ?: 0) + item.quantity,
                addedAt = existing?.addedAt ?: now(),
            ),
        )
    }

    override suspend fun setQuantity(productId: String, quantity: Int) {
        // Zero is a removal, not a row holding nothing. Leaving it would show "0 x Coffee".
        if (quantity <= 0) {
            cartDao.remove(productId)
            return
        }
        val existing = cartDao.item(productId) ?: return
        cartDao.upsert(existing.copy(quantity = quantity))
    }

    override suspend fun remove(productId: String) = cartDao.remove(productId)

    override suspend fun clear() = cartDao.clear()
}

private fun CartItemEntity.toDomain() = CartItem(
    productId = productId,
    name = name,
    price = price,
    quantity = quantity,
)
