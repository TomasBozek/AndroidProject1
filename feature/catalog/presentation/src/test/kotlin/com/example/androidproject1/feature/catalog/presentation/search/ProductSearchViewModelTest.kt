package com.example.androidproject1.feature.catalog.presentation.search

import com.example.androidproject1.core.domain.error.NetworkError
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import com.example.androidproject1.feature.catalog.domain.test.FakeRecentSearchesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val catalogRepository = FakeCatalogRepository(products = listOf(COFFEE, TEA))
    private val recentSearchesRepository = FakeRecentSearchesRepository()

    /**
     * The debounce is virtual time, so `Dispatchers.Main` has to share `runTest`'s scheduler: the
     * rule's own `UnconfinedTestDispatcher` has a scheduler of its own, and `advanceTimeBy` would
     * drive the wrong clock. Every test here therefore runs the ViewModel's `init` explicitly with
     * `advanceUntilIdle()` rather than finding it already done.
     */
    private fun TestScope.viewModel(): ProductSearchViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return ProductSearchViewModel(
            logger = FakeLogger(),
            catalogRepository = catalogRepository,
            recentSearchesRepository = recentSearchesRepository,
        )
    }

    @Test
    fun `starts on an empty field with nothing searched`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        val state = viewModel.state.value

        assertEquals("", state.data?.query)
        assertEquals(false, state.data?.searched)
        assertNull(state.loading)
    }

    @Test
    fun `the field echoes every keystroke without waiting for the debounce`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(ProductSearchEvent.QueryChanged("cof"))

        // No advance: the field is echoed on the keystroke, not after the debounce.
        assertEquals("cof", viewModel.state.value.data?.query)
    }

    @Test
    fun `six keystrokes are one query`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        catalogRepository.searchedFor.clear()

        "coffee".forEachIndexed { index, _ ->
            viewModel.onUiEvent(ProductSearchEvent.QueryChanged("coffee".take(index + 1)))
            advanceTimeBy(50)
        }
        advanceUntilIdle()

        assertEquals(listOf("coffee"), catalogRepository.searchedFor)
    }

    @Test
    fun `nothing is searched for until the debounce has passed`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()
        catalogRepository.searchedFor.clear()

        viewModel.onUiEvent(ProductSearchEvent.QueryChanged("cof"))
        advanceTimeBy(200)
        assertTrue(catalogRepository.searchedFor.isEmpty())

        advanceTimeBy(200)
        assertEquals(listOf("cof"), catalogRepository.searchedFor)
    }

    @Test
    fun `a search shows what it matched and records it`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(ProductSearchEvent.QueryChanged("cof"))
        advanceUntilIdle()

        assertEquals(listOf(COFFEE), viewModel.state.value.data?.results)
        assertEquals(true, viewModel.state.value.data?.searched)
        assertEquals(listOf("cof"), recentSearchesRepository.recents.value)
    }

    @Test
    fun `tapping a recent search fills the field and searches again`() = runTest {
        val viewModel = viewModel()
        recentSearchesRepository.recents.value = listOf("tea")

        viewModel.onUiEvent(ProductSearchEvent.RecentClicked("tea"))
        advanceUntilIdle()

        assertEquals("tea", viewModel.state.value.data?.query)
        assertEquals(listOf(TEA), viewModel.state.value.data?.results)
    }

    @Test
    fun `clearing the recents empties them`() = runTest {
        val viewModel = viewModel()
        recentSearchesRepository.recents.value = listOf("tea")

        viewModel.onUiEvent(ProductSearchEvent.ClearRecentsClicked)
        advanceUntilIdle()

        assertEquals(emptyList<String>(), viewModel.state.value.data?.recents)
    }

    @Test
    fun `a failing results load is shown under the results id`() = runTest {
        catalogRepository.failWith = NetworkError()
        val viewModel = viewModel()

        viewModel.onUiEvent(ProductSearchEvent.QueryChanged("cof"))
        advanceUntilIdle()

        assertEquals("results", (viewModel.state.value.content as? ContentState.Error)?.id)
    }

    @Test
    fun `a failing recents load leaves the results working`() = runTest {
        // The point of a content id per load: the failure belongs to recents, and the retry the
        // user is offered re-runs that one rather than whichever call happened to be last.
        recentSearchesRepository.failWith = NetworkError()
        val viewModel = viewModel()

        viewModel.onUiEvent(ProductSearchEvent.QueryChanged("cof"))
        advanceUntilIdle()

        assertEquals("recents", (viewModel.state.value.content as? ContentState.Error)?.id)
        assertEquals(listOf(COFFEE), viewModel.state.value.data?.results)
    }

    @Test
    fun `tapping a product navigates to it`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(ProductSearchEvent.ProductClicked(COFFEE))

        assertEquals(
            ProductSearchNavigation.ProductDetail(COFFEE.id),
            viewModel.navigation.first(),
        )
    }

    private companion object {

        val COFFEE = FakeCatalogRepository.COFFEE

        val TEA = Product(
            id = "tea",
            categoryId = "beverages",
            name = "Tea",
            price = 300,
            description = "Loose leaf.",
        )
    }
}
