package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeCatalogRepository = FakeCatalogRepository()) =
        ProductPickerViewModel(
            logger = FakeLogger(),
            // The route key the destination hands in. It carries the requester's result key, so
            // the picker survives process death still knowing who asked.
            args = ProductPickerDestination(resultKey = "cart_picked_product"),
            catalogRepository = repository,
        )

    @Test
    fun `shows everything the catalog has cached`() = runTest {
        val state = viewModel().state.value

        assertEquals(listOf(FakeCatalogRepository.COFFEE), state.data?.products)
    }

    @Test
    fun `the result key comes from the route, not from state`() = runTest {
        assertEquals("cart_picked_product", viewModel().resultKey)
    }

    @Test
    fun `picking a product reports its id and nothing else`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProductPickerEvent.ProductClicked(FakeCatalogRepository.COFFEE))

        // An id, not the product: what the requester does with it is the requester's business.
        assertEquals(
            ProductPickerNavigation.Picked(FakeCatalogRepository.COFFEE.id),
            viewModel.navigation.first(),
        )
    }
}
