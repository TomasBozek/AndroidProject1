package com.example.androidproject1.feature.movies.presentation.moviedetail

import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import com.example.androidproject1.feature.movies.domain.MoviesRepository
import com.example.androidproject1.service.core.domain.error.NotFoundError
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.ui.test.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The route key the destination hands in. A plain object on Navigation 3 — no Bundle, and so
    // no Robolectric.
    private fun viewModel(repository: FakeMoviesRepository = FakeMoviesRepository()) = MovieDetailViewModel(
        logger = FakeLogger(),
        args = MovieDetailDestination(movieId = MOVIE_ID),
        moviesRepository = repository,
    )

    @Test
    fun `loads the movie named by the route, without being told to`() = runTest {
        val repository = FakeMoviesRepository()

        val state = viewModel(repository).state.value

        assertEquals(listOf(MOVIE_ID), repository.asked)
        assertEquals(MovieDetailState.PREVIEW.detail, state.data?.detail)
        assertNull("the overlay is down once the read returns", state.loading)
        assertNull(state.content)
    }

    @Test
    fun `a movie that cannot be read shows the error in place of the screen`() = runTest {
        val state = viewModel(FakeMoviesRepository(failing = true)).state.value

        assertNull(state.data)
        assertNotNull(state.content)
    }

    @Test
    fun `up navigates up`() = runTest {
        val viewModel = viewModel()

        viewModel.onUiEvent(MovieDetailEvent.NavigateUpClicked)

        assertEquals(MovieDetailNavigation.NavigateUp, viewModel.navigation.first())
    }

    private class FakeMoviesRepository(private val failing: Boolean = false) : MoviesRepository {

        val asked = mutableListOf<Int>()

        override fun observePage(page: Int): Flow<Outcome<MoviePage>> = emptyFlow()

        override suspend fun refresh(): Outcome<Unit> = Outcome.Success(Unit)

        override suspend fun getMovie(id: Int): Outcome<MovieDetail> {
            asked += id
            return if (failing) Outcome.Failure(NotFoundError()) else Outcome.Success(MovieDetailState.PREVIEW.detail)
        }
    }

    private companion object {

        const val MOVIE_ID = 550
    }
}
