package com.example.androidproject1.feature.catalog.presentation

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repository: FakeCatalogRepository,
        productId: String = "coffee",
    ) = ProductDetailViewModel(
        logger = FakeLogger(),
        // The route key the destination hands in. A plain object on Navigation 3 — no Bundle, and
        // so no Robolectric.
        args = ProductDetailDestination(productId = productId),
        catalogRepository = repository,
    )

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
}
