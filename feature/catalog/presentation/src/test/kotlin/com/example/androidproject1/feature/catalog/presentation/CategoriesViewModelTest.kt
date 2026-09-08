package com.example.androidproject1.feature.catalog.presentation

import app.cash.turbine.test
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.test.FakeLogger
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeCatalogRepository) =
        CategoriesViewModel(logger = FakeLogger(), catalogRepository = repository)

    @Test
    fun `loads the catalog and clears the loading overlay`() = runTest {
        val state = viewModel(FakeCatalogRepository()).state.value

        assertEquals(1, state.data?.categories?.size)
        assertNull(state.loading)
    }

    @Test
    fun `a failure to load shows a retryable message instead of the content`() = runTest {
        val state = viewModel(FakeCatalogRepository.offline()).state.value

        // Inline, not a dialog: there would be nothing behind the dialog to look at.
        val content = state.content
        assertTrue(content is ContentState.Error)
        assertNull(state.alert)
        // ...and it offers a way out.
        assertTrue(content!!.actionLabel != null)
    }

    @Test
    fun `retrying re-runs the failed load`() = runTest {
        val repository = FakeCatalogRepository.offline()
        val viewModel = viewModel(repository)
        val contentId = viewModel.state.value.content!!.id

        repository.failWith = null
        viewModel.onSystemEvent(
            com.example.androidproject1.core.ui.event.SystemEvent.ContentAction(contentId),
        )

        assertNull(viewModel.state.value.content)
        assertEquals(1, viewModel.state.value.data?.categories?.size)
    }

    @Test
    fun `tapping a category navigates to its products`() = runTest {
        val viewModel = viewModel(FakeCatalogRepository())
        val category = viewModel.state.value.data!!.categories.first()

        viewModel.navigation.test {
            viewModel.onUiEvent(CategoriesEvent.CategoryClicked(category))

            val navigation = awaitItem()
            assertTrue(navigation is CategoriesNavigation.Products)
            assertEquals("beverages", (navigation as CategoriesNavigation.Products).categoryId)
        }
    }
}
