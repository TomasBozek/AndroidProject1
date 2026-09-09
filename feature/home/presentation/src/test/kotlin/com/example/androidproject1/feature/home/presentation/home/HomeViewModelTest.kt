package com.example.androidproject1.feature.home.presentation.home

import com.example.androidproject1.core.domain.test.FakeLogger
import com.example.androidproject1.core.ui.event.SystemEvent
import com.example.androidproject1.core.ui.event.UiCommand
import com.example.androidproject1.core.ui.test.MainDispatcherRule
import com.example.androidproject1.core.ui.text.toUiText
import com.example.androidproject1.feature.catalog.domain.Product
import com.example.androidproject1.feature.catalog.domain.test.FakeFavouritesRepository
import com.example.androidproject1.feature.home.presentation.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val coffee = Product(
        id = "coffee",
        categoryId = "beverages",
        name = "Coffee",
        price = 450,
        description = "Freshly ground, brewed to order.",
    )

    private fun repository(favourites: List<Product> = listOf(coffee)) =
        FakeFavouritesRepository(initial = favourites).apply { known = listOf(coffee) }

    private fun viewModel(repository: FakeFavouritesRepository = repository()) =
        HomeViewModel(logger = FakeLogger(), favouritesRepository = repository)

    @Test
    fun `renders immediately, with no loading overlay`() = runTest {
        val state = viewModel(repository(favourites = emptyList())).state.value

        assertEquals(R.string.home_greeting.toUiText(), state.data?.greeting)
        // The favourites flow must not raise the overlay — the greeting is renderable at once.
        assertNull(state.loading)
    }

    @Test
    fun `shows the favourites it observes`() = runTest {
        val state = viewModel().state.value

        assertEquals(listOf(coffee), state.data?.favourites)
    }

    @Test
    fun `removing a favourite takes it out of the list`() = runTest {
        val repository = repository()
        val viewModel = viewModel(repository)

        viewModel.onUiEvent(HomeEvent.FavouriteRemoved("coffee"))

        assertEquals(emptyList<Product>(), repository.current)
        assertEquals(emptyList<Product>(), viewModel.state.value.data?.favourites)
    }

    @Test
    fun `removing a favourite offers an undo`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(HomeEvent.FavouriteRemoved("coffee"))

        val command = viewModel.command.first()
        assertTrue(command is UiCommand.ShowSnackbar)
        command as UiCommand.ShowSnackbar
        assertEquals(HomeViewModel.SNACKBAR_UNDO_REMOVE, command.id)
        assertEquals(R.string.home_favourite_undo.toUiText(), command.actionLabel)
    }

    @Test
    fun `undo puts the favourite back`() = runTest {
        val repository = repository()
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(HomeEvent.FavouriteRemoved("coffee"))

        viewModel.onSystemEvent(SystemEvent.SnackbarAction(HomeViewModel.SNACKBAR_UNDO_REMOVE))

        assertEquals(listOf(coffee), repository.current)
        assertEquals(listOf(coffee), viewModel.state.value.data?.favourites)
    }

    @Test
    fun `undo fires once, not on every later snackbar`() = runTest {
        val repository = repository()
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(HomeEvent.FavouriteRemoved("coffee"))
        viewModel.onSystemEvent(SystemEvent.SnackbarAction(HomeViewModel.SNACKBAR_UNDO_REMOVE))
        viewModel.onUiEvent(HomeEvent.FavouriteRemoved("coffee"))

        // A second undo with nothing remembered must not resurrect the earlier removal.
        viewModel.onSystemEvent(SystemEvent.SnackbarAction(HomeViewModel.SNACKBAR_UNDO_REMOVE))
        viewModel.onSystemEvent(SystemEvent.SnackbarAction(HomeViewModel.SNACKBAR_UNDO_REMOVE))

        assertEquals(listOf(coffee), repository.current)
    }
}
