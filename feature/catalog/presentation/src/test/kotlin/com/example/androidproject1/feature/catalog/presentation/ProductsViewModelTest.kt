package com.example.androidproject1.feature.catalog.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs under Robolectric because [androidx.navigation.toRoute] decodes through an
 * `android.os.Bundle`, which a plain JVM test does not have. Only screens that take navigation
 * arguments need this; the rest stay on the JVM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // What the framework would restore into the ViewModel after process death.
    private fun savedState() = SavedStateHandle(
        mapOf("categoryId" to "beverages", "categoryName" to "Beverages"),
    )

    private fun viewModel(repository: FakeCatalogRepository) = ProductsViewModel(
        logger = FakeLogger(),
        savedStateHandle = savedState(),
        catalogRepository = repository,
    )

    @Test
    fun `loads the category named by the route, without being told to`() = runTest {
        val repository = FakeCatalogRepository()

        val state = viewModel(repository).state.value

        // The load happens in init from the route, not from a LaunchedEffect in the destination.
        assertEquals(1, repository.getProductsCallCount)
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
