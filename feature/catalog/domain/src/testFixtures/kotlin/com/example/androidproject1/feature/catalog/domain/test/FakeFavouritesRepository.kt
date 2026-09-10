package com.example.androidproject1.feature.catalog.domain.test

import com.example.androidproject1.feature.catalog.domain.FavouritesRepository
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [FavouritesRepository], here rather than in either feature's test source set because
 * both `:feature:catalog` and `:feature:home` need it.
 *
 * Backed by a `MutableStateFlow`, so a test that toggles a favourite sees the observing collector
 * react the way the database would.
 */
class FakeFavouritesRepository(
    initial: List<Product> = emptyList(),
    var failWith: DomainError? = null,
) : FavouritesRepository {

    private val favourites = MutableStateFlow(initial)

    /** Every product this fake knows about, so an id can be turned back into a [Product]. */
    var known: List<Product> = initial

    val current: List<Product> get() = favourites.value

    override fun observeFavourites(): Flow<Outcome<List<Product>>> =
        favourites.map { failWith?.let { error -> Outcome.Failure(error) } ?: Outcome.Success(it) }

    override fun observeIsFavourite(productId: String): Flow<Outcome<Boolean>> =
        favourites.map { list ->
            failWith?.let { Outcome.Failure(it) } ?: Outcome.Success(list.any { it.id == productId })
        }

    override suspend fun setFavourite(productId: String, favourite: Boolean): Outcome<Unit> {
        failWith?.let { return Outcome.Failure(it) }
        favourites.value = if (favourite) {
            val product = known.find { it.id == productId } ?: return Outcome.Success(Unit)
            // Most recent first, matching the DAO's ORDER BY.
            listOf(product) + favourites.value.filterNot { it.id == productId }
        } else {
            favourites.value.filterNot { it.id == productId }
        }
        return Outcome.Success(Unit)
    }
}
