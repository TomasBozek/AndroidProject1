package com.example.androidproject1.feature.catalog.presentation.categories

import app.cash.turbine.test
import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.state.ContentState
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.feature.catalog.domain.test.FakeCatalogRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    @Test
    fun `a refresh failure over a stale cache keeps the list on screen`() = runTest {
        val repository = FakeCatalogRepository(
            failWith = com.example.androidproject1.core.domain.error.NetworkError(),
            staleThenFail = true,
        )

        val state = viewModel(repository).state.value

        // What `cached` produces when the network is down over a populated cache. Replacing a
        // usable list with "try again" is the regression this guards.
        assertEquals(1, state.data?.categories?.size)
        assertNull(state.content)
    }

    @Test
    fun `a stale list says so rather than failing silently`() = runTest {
        val repository = FakeCatalogRepository(
            failWith = com.example.androidproject1.core.domain.error.NetworkError(),
            staleThenFail = true,
        )
        val viewModel = viewModel(repository)

        val command = viewModel.command.first()

        assertTrue(command is com.example.androidproject1.core.ui.event.UiCommand.ShowSnackbar)
    }

    @Test
    fun `a failure with nothing cached still shows the error state`() = runTest {
        val state = viewModel(FakeCatalogRepository.offline()).state.value

        // Nothing to keep, so the retryable message is the only useful thing to show.
        assertNull(state.data)
        assertNotNull(state.content)
    }
}
