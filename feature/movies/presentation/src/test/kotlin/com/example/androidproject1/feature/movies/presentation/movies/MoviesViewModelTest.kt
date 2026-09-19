package com.example.androidproject1.feature.movies.presentation.movies

import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import com.example.androidproject1.feature.movies.domain.MoviesRepository
import com.example.androidproject1.service.core.domain.error.NetworkError
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.event.UiCommand
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * What the state becomes as pages arrive, fail and are refreshed. The repository is a fake with
 * one page size and one failing page, because the interesting part is the view model's
 * bookkeeping — which page is next, when the end is reached, what a refresh throws away.
 */
class MoviesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeMoviesRepository) =
        MoviesViewModel(logger = FakeLogger(), moviesRepository = repository)

    @Test
    fun `renders an empty list immediately and fills it with page 1`() = runTest {
        val repository = FakeMoviesRepository()

        val state = viewModel(repository).state.value

        assertEquals(listOf(1), repository.observed)
        assertEquals(titles(1, PAGE_SIZE), state.data?.movies?.map { it.title })
        assertEquals(1, state.data?.page)
        assertEquals(PAGES, state.data?.totalPages)
        assertTrue(state.data?.loaded == true)
        assertFalse(state.data?.endReached == true)
        assertNull("a cache hit ends the overlay on the first emission", state.loading)
        assertNull(state.alert)
        assertNull(state.content)
    }

    @Test
    fun `loading more appends the next page and stops at the last`() = runTest {
        val viewModel = viewModel(FakeMoviesRepository())

        viewModel.onUiEvent(MoviesEvent.LoadMore)
        assertEquals(titles(1, 2 * PAGE_SIZE), viewModel.state.value.data?.movies?.map { it.title })
        assertEquals(2, viewModel.state.value.data?.page)

        viewModel.onUiEvent(MoviesEvent.LoadMore)
        assertEquals(3, viewModel.state.value.data?.page)
        assertTrue(viewModel.state.value.data?.endReached == true)

        // Nothing past the last page: the ask is ignored rather than answered with a 422.
        viewModel.onUiEvent(MoviesEvent.LoadMore)
        assertEquals(3, viewModel.state.value.data?.page)
        assertFalse(viewModel.state.value.data?.loadingMore == true)
    }

    @Test
    fun `a page that fails leaves the pages before it and reports beside them`() = runTest {
        val viewModel = viewModel(FakeMoviesRepository(failingPage = 2))

        viewModel.onUiEvent(MoviesEvent.LoadMore)

        val state = viewModel.state.value
        assertEquals(titles(1, PAGE_SIZE), state.data?.movies?.map { it.title })
        assertEquals(1, state.data?.page)
        assertFalse("the footer spinner is gone", state.data?.loadingMore == true)
        assertNull("the list stays; no inline error replaces it", state.content)
        assertNotNull("a network error on a later page is an alert", state.alert)
    }

    @Test
    fun `a first page that fails with nothing cached shows the error in place of the list`() = runTest {
        val state = viewModel(FakeMoviesRepository(failingPage = 1)).state.value

        assertNotNull(state.content)
        assertFalse(state.data?.loaded == true)
    }

    @Test
    fun `a stale first page says so rather than failing silently`() = runTest {
        val viewModel = viewModel(FakeMoviesRepository(failingPage = 1, staleThenFail = true))

        assertEquals(titles(1, PAGE_SIZE), viewModel.state.value.data?.movies?.map { it.title })
        assertNull(viewModel.state.value.content)
        assertTrue(viewModel.command.first() is UiCommand.ShowSnackbar)
    }

    @Test
    fun `a refresh calls the repository and keeps only page 1`() = runTest {
        val repository = FakeMoviesRepository()
        val viewModel = viewModel(repository)
        viewModel.onUiEvent(MoviesEvent.LoadMore)
        assertEquals(2, viewModel.state.value.data?.page)

        viewModel.onUiEvent(MoviesEvent.Refresh)

        assertEquals(1, repository.refreshes)
        assertEquals(1, viewModel.state.value.data?.page)
        assertEquals(titles(1, PAGE_SIZE), viewModel.state.value.data?.movies?.map { it.title })
        assertFalse("the indicator is down once the call returns", viewModel.state.value.data?.refreshing == true)
        assertNull("the refresh is the indicator, not the overlay", viewModel.state.value.loading)
    }

    @Test
    fun `a refresh that fails drops nothing`() = runTest {
        val viewModel = viewModel(FakeMoviesRepository(refreshFails = true))
        viewModel.onUiEvent(MoviesEvent.LoadMore)

        viewModel.onUiEvent(MoviesEvent.Refresh)

        assertEquals(2, viewModel.state.value.data?.page)
        assertFalse(viewModel.state.value.data?.refreshing == true)
        assertNotNull(viewModel.state.value.alert)
    }

    @Test
    fun `a row opens its detail`() = runTest {
        val viewModel = viewModel(FakeMoviesRepository())

        viewModel.onUiEvent(MoviesEvent.MovieClicked(7))

        assertEquals(MoviesNavigation.MovieDetail(7), viewModel.navigation.first())
    }

    @Test
    fun `up navigates up`() = runTest {
        val viewModel = viewModel(FakeMoviesRepository())

        viewModel.onUiEvent(MoviesEvent.NavigateUpClicked)

        assertEquals(MoviesNavigation.NavigateUp, viewModel.navigation.first())
    }

    private fun titles(from: Int, to: Int) = (from..to).map { "Movie $it" }

    /**
     * Three pages of [PAGE_SIZE], one of which may fail; a refresh that may fail. Emits the way
     * `cached()` does — the page once, or the page and then the failure when `staleThenFail`.
     */
    private class FakeMoviesRepository(
        private val failingPage: Int? = null,
        private val staleThenFail: Boolean = false,
        private val refreshFails: Boolean = false,
    ) : MoviesRepository {

        val observed = mutableListOf<Int>()
        var refreshes = 0

        override fun observePage(page: Int): Flow<Outcome<MoviePage>> = flow {
            observed += page
            if (page == failingPage) {
                if (staleThenFail) emit(Outcome.Success(page(page)))
                emit(Outcome.Failure(NetworkError()))
            } else {
                emit(Outcome.Success(page(page)))
            }
        }

        override suspend fun refresh(): Outcome<Unit> {
            refreshes++
            return if (refreshFails) Outcome.Failure(NetworkError()) else Outcome.Success(Unit)
        }

        override suspend fun getMovie(id: Int): Outcome<MovieDetail> = Outcome.Failure(NetworkError())

        private fun page(page: Int) = MoviePage(
            page = page,
            movies = (1..PAGE_SIZE).map { movie((page - 1) * PAGE_SIZE + it) },
            totalPages = PAGES,
        )

        private fun movie(id: Int) = Movie(
            id = id,
            title = "Movie $id",
            overview = "",
            posterUrl = null,
            releaseDate = LocalDate.of(2000, 1, 1),
            rating = 7.0,
        )
    }

    private companion object {

        const val PAGE_SIZE = 2
        const val PAGES = 3
    }
}
