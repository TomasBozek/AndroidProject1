package com.example.androidproject1.feature.catalog.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.androidproject1.core.ui.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repository: FakeCatalogRepository,
        productId: String = "coffee",
    ) = ProductDetailViewModel(
        logger = FakeLogger(),
        savedStateHandle = SavedStateHandle(mapOf("productId" to productId)),
        catalogRepository = repository,
    )

    @Test
    fun `loads the product named by the route`() = runTest {
        val state = viewModel(FakeCatalogRepository()).state.value

        assertEquals(FakeCatalogRepository.COFFEE, state.data?.product)
        assertNull(state.loading)
    }

    @Test
    fun `a product that no longer exists renders as a null product`() = runTest {
        val state = viewModel(FakeCatalogRepository(), productId = "gone").state.value

        // Success with no match, so this is a state the screen renders — not an error.
        assertNull(state.data?.product)
        assertNull(state.content)
    }
}
