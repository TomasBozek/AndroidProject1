package com.example.androidproject1.feature.cart.presentation.cart

import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.cart.domain.CartItem
import com.example.androidproject1.feature.cart.domain.test.FakeCartRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.SystemEvent
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import com.example.androidproject1.service.core.ui.text.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric because the plural assertions resolve against the real resource table — the
 * behaviour worth testing is that the quantity picks the form, which a fake cannot show.
 */
@RunWith(RobolectricTestRunner::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private val coffee = CartItem("coffee", "Coffee", 450, 2)
    private val croissant = CartItem("croissant", "Croissant", 275, 1)

    private fun viewModel(repository: FakeCartRepository = FakeCartRepository()) =
        CartViewModel(logger = FakeLogger(), cartRepository = repository)

    @Test
    fun `an empty cart renders straight away, with no overlay`() = runTest {
        val state = viewModel().state.value

        assertTrue(state.data!!.isEmpty)
        assertNull(state.loading)
    }

    @Test
    fun `the total is the sum of the lines`() = runTest {
        val state = viewModel(FakeCartRepository(listOf(coffee, croissant))).state.value

        // 450 x 2 + 275 x 1
        assertEquals(1175L, state.data!!.total)
    }

    @Test
    fun `one item reads as singular`() = runTest {
        val state = viewModel(FakeCartRepository(listOf(croissant))).state.value

        assertEquals("1 item", state.data!!.itemCountLabel.resolve(context))
    }

    @Test
    fun `two items read as plural`() = runTest {
        val state = viewModel(FakeCartRepository(listOf(coffee))).state.value

        assertEquals("2 items", state.data!!.itemCountLabel.resolve(context))
    }

    @Test
    fun `five items read as plural too`() = runTest {
        // English has two forms; Czech (D24) has four, and 5 is the one that differs from 2.
        val state = viewModel(FakeCartRepository(listOf(coffee.copy(quantity = 5)))).state.value

        assertEquals("5 items", state.data!!.itemCountLabel.resolve(context))
    }

    @Test
    fun `changing a quantity writes it through`() = runTest {
        val repository = FakeCartRepository(listOf(coffee))
        val viewModel = viewModel(repository)

        viewModel.onUiEvent(CartEvent.QuantityChanged("coffee", 4))

        assertEquals(4, repository.current.single().quantity)
    }

    @Test
    fun `dropping a quantity to zero removes the line`() = runTest {
        val repository = FakeCartRepository(listOf(coffee))

        viewModel(repository).onUiEvent(CartEvent.QuantityChanged("coffee", 0))

        assertEquals(emptyList<CartItem>(), repository.current)
    }

    @Test
    fun `removing an item offers an undo that puts it back`() = runTest {
        val repository = FakeCartRepository(listOf(coffee))
        val viewModel = viewModel(repository)

        viewModel.onUiEvent(CartEvent.ItemRemoved("coffee"))
        assertEquals(emptyList<CartItem>(), repository.current)

        viewModel.onSystemEvent(SystemEvent.SnackbarAction(CartViewModel.SNACKBAR_UNDO_REMOVE))

        // The quantity comes back too, not just the product.
        assertEquals(listOf(coffee), repository.current)
    }

    @Test
    fun `checkout asks before it empties the cart`() = runTest {
        val repository = FakeCartRepository(listOf(coffee))
        val viewModel = viewModel(repository)

        viewModel.onUiEvent(CartEvent.CheckoutClicked)

        // Nothing has happened yet: an order is not something to do on one tap.
        assertNotNull(viewModel.state.value.alert)
        assertEquals(listOf(coffee), repository.current)
    }

    @Test
    fun `the checkout dialog carries its own title, not the error one`() = runTest {
        val viewModel = viewModel(FakeCartRepository(listOf(coffee)))

        viewModel.onUiEvent(CartEvent.CheckoutClicked)

        // AlertState.title has no default precisely so a confirmation is not labelled
        // "Something went wrong".
        assertEquals("Place this order?", viewModel.state.value.alert!!.title!!.resolve(context))
    }

    @Test
    fun `confirming checkout empties the cart`() = runTest {
        val repository = FakeCartRepository(listOf(coffee))
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(CartEvent.CheckoutClicked)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Confirmed(CartViewModel.ALERT_ID_CHECKOUT))

        assertEquals(emptyList<CartItem>(), repository.current)
    }

    @Test
    fun `declining checkout leaves the cart alone`() = runTest {
        val repository = FakeCartRepository(listOf(coffee))
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(CartEvent.CheckoutClicked)

        viewModel.onSystemEvent(SystemEvent.AlertResult.Declined(CartViewModel.ALERT_ID_CHECKOUT))

        assertEquals(listOf(coffee), repository.current)
    }

    @Test
    fun `checkout on an empty cart does nothing at all`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(CartEvent.CheckoutClicked)

        assertNull(viewModel.state.value.alert)
    }

    @Test
    fun `adding the same product twice increases its quantity`() = runTest {
        val repository = FakeCartRepository(listOf(croissant))
        val viewModel = viewModel(repository)

        viewModel.onProductPicked("croissant") { croissant.copy(quantity = 1) }

        assertEquals(2, repository.current.single().quantity)
    }

    @Test
    fun `picking a product the catalog no longer has adds nothing`() = runTest {
        val repository = FakeCartRepository()
        val viewModel = viewModel(repository)

        viewModel.onProductPicked("gone") { null }

        assertEquals(emptyList<CartItem>(), repository.current)
    }

    @Test
    fun `add item asks to open the picker rather than opening it`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(CartEvent.AddItemClicked)

        assertEquals(CartNavigation.PickProduct, viewModel.navigation.first())
    }

    @Test
    fun `the checkout message resolves the count rather than printing the object`() = runTest {
        val viewModel = viewModel(FakeCartRepository(listOf(coffee)))

        viewModel.onUiEvent(CartEvent.CheckoutClicked)

        // A UiText passed as a format argument used to reach String.format as an object and render
        // as "Plural(id=…, quantity=2, args=[2]) will be ordered" — which compiles, type-checks,
        // and is visible only on screen.
        assertEquals(
            "2 items will be ordered and your cart emptied.",
            viewModel.state.value.alert!!.message!!.resolve(context),
        )
    }
}
