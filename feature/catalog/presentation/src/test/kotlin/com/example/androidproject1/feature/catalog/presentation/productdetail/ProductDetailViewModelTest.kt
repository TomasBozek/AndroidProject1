package com.example.androidproject1.feature.catalog.presentation.productdetail

import com.example.androidproject1.feature.cart.domain.AddProductToCart
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.domain.test.FakeCartRepository
import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import com.example.androidproject1.feature.catalog.domain.test.FakeFavouritesRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun favourites(favourited: Boolean = false) = FakeFavouritesRepository(
        initial = if (favourited) listOf(FakeCatalogRepository.COFFEE) else emptyList(),
    ).apply { known = listOf(FakeCatalogRepository.COFFEE) }

    private fun viewModel(
        repository: FakeCatalogRepository,
        productId: String = "coffee",
        favouritesRepository: FakeFavouritesRepository = favourites(),
        cartRepository: FakeCartRepository = FakeCartRepository(),
    ) = ProductDetailViewModel(
        logger = FakeLogger(),
        // The route key the destination hands in. A plain object on Navigation 3 — no Bundle, and
        // so no Robolectric.
        args = ProductDetailDestination(productId = productId),
        catalogRepository = repository,
        favouritesRepository = favouritesRepository,
        addProductToCart = AddProductToCart(cartRepository),
    )

    @Test
    fun `a product that is not favourited shows an empty heart`() = runTest {
        val state = viewModel(FakeCatalogRepository()).state.value

        assertEquals(false, state.data?.isFavourite)
    }

    @Test
    fun `a product that is favourited shows a filled heart`() = runTest {
        val state = viewModel(
            FakeCatalogRepository(),
            favouritesRepository = favourites(favourited = true),
        ).state.value

        assertEquals(true, state.data?.isFavourite)
    }

    @Test
    fun `tapping the heart favourites the product`() = runTest {
        val favourites = favourites()
        val viewModel = viewModel(FakeCatalogRepository(), favouritesRepository = favourites)

        viewModel.onUiEvent(ProductDetailEvent.FavouriteToggled)

        assertEquals(listOf(FakeCatalogRepository.COFFEE), favourites.current)
        // The heart follows the database rather than the tap, so the observed flow is what
        // updated the state here.
        assertEquals(true, viewModel.state.value.data?.isFavourite)
    }

    @Test
    fun `tapping the heart again removes the favourite`() = runTest {
        val favourites = favourites(favourited = true)
        val viewModel = viewModel(FakeCatalogRepository(), favouritesRepository = favourites)

        viewModel.onUiEvent(ProductDetailEvent.FavouriteToggled)

        assertEquals(emptyList<Any>(), favourites.current)
        assertEquals(false, viewModel.state.value.data?.isFavourite)
    }

    @Test
    fun `loads the product named by the route`() = runTest {
        val state = viewModel(FakeCatalogRepository()).state.value

        assertEquals(FakeCatalogRepository.COFFEE, state.data?.product)
        assertNull(state.loading)
    }

    @Test
    fun `a product that no longer exists shows an empty content state`() = runTest {
        val state = viewModel(FakeCatalogRepository(), productId = "gone").state.value

        // Success with no match, so it is Empty rather than Error — the same distinction
        // ProductsViewModel draws for a category with no products.
        val content = state.content
        assertTrue(content is ContentState.Empty)
        assertEquals(ProductDetailViewModel.CONTENT_NOT_FOUND, content?.id)
        assertNull(state.data)
    }

    @Test
    fun `the empty state's action leaves the screen`() = runTest {
        val viewModel = viewModel(FakeCatalogRepository(), productId = "gone")

        viewModel.onSystemEvent(SystemEvent.ContentAction(ProductDetailViewModel.CONTENT_NOT_FOUND))

        assertEquals(UiCommand.NavigateBack, viewModel.command.first())
    }

    @Test
    fun `adding to the cart writes the line here, not in the nav host`() = runTest {
        val cart = FakeCartRepository()
        val viewModel = viewModel(FakeCatalogRepository(), cartRepository = cart)

        viewModel.onUiEvent(ProductDetailEvent.AddToCartClicked)

        // The write runs in this ViewModel's scope, which survives the rotation that used to
        // cancel it: the nav host launched it in `rememberCoroutineScope`.
        assertEquals(
            listOf(
                CartItem(
                    productId = "coffee",
                    name = FakeCatalogRepository.COFFEE.name,
                    price = FakeCatalogRepository.COFFEE.price,
                    quantity = 1,
                ),
            ),
            cart.current,
        )
    }

    @Test
    fun `adding to the cart before the product has loaded does nothing`() = runTest {
        val cart = FakeCartRepository()
        // A product id the fake does not know, so the state stays null.
        val viewModel = viewModel(FakeCatalogRepository(), productId = "nothing", cartRepository = cart)

        viewModel.onUiEvent(ProductDetailEvent.AddToCartClicked)

        assertEquals(emptyList<CartItem>(), cart.current)
    }

    @Test
    fun `the heart keeps its value when the product arrives after it`() = runTest {
        // The two reads race. Delaying the product puts the favourite flag first, which is the
        // order that used to lose it: the flag was dropped while `data` was still null, and then
        // the load wrote a fresh state with the heart empty.
        val repository = FakeCatalogRepository(getProductDelayMillis = 10)

        val viewModel = viewModel(repository, favouritesRepository = favourites(favourited = true))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.data?.isFavourite)
    }

    @Test
    fun `the heart keeps its value when the product arrives before it`() = runTest {
        // The other order, so neither writer wins by luck.
        val viewModel = viewModel(FakeCatalogRepository(), favouritesRepository = favourites(favourited = true))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.data?.isFavourite)
    }
}
