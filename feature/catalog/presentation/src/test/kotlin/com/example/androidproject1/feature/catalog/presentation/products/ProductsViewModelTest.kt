package com.example.androidproject1.feature.catalog.presentation.products

import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.state.ContentState
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The route key the destination hands in. A plain object on Navigation 3 — no Bundle, and so
    // no Robolectric.
    private val route = ProductsDestination(categoryId = "beverages", categoryName = "Beverages")

    private fun viewModel(repository: FakeCatalogRepository) = ProductsViewModel(
        logger = FakeLogger(),
        args = route,
        catalogRepository = repository,
    )

    @Test
    fun `loads the category named by the route, without being told to`() = runTest {
        val repository = FakeCatalogRepository()

        val state = viewModel(repository).state.value

        // The load happens in init from the route, not from a LaunchedEffect in the destination.
        assertEquals(1, repository.observeProductsCallCount)
        assertEquals("Beverages", state.data?.categoryName)
        assertEquals(listOf(FakeCatalogRepository.COFFEE), state.data?.products)
        assertNull(state.loading)
    }

    @Test
    fun `a category with no products shows an empty state`() = runTest {
        val repository = FakeCatalogRepository(products = emptyList())

        val state = viewModel(repository).state.value

        assertTrue(state.content is ContentState.Empty)
        assertNull(state.data)
    }

    @Test
    fun `a failure shows a retryable message`() = runTest {
        val state = viewModel(FakeCatalogRepository.offline()).state.value

        assertTrue(state.content is ContentState.Error)
        assertNull(state.alert)
    }
}
