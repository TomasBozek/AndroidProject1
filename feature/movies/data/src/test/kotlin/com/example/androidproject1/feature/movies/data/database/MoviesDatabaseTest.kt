package com.example.androidproject1.feature.movies.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.movies.data.source.DefaultLocalMoviesDataSource
import com.example.androidproject1.feature.movies.domain.Movie
import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

/**
 * The DAO round trip, against a real SQLite rather than a fake.
 *
 * What is worth testing is the SQL: that a page comes back in the server's order, that replacing
 * page 2 leaves page 1 alone, and that the refresh leaves exactly page 1 — a fake DAO would
 * assert that this test's own map lookup works.
 */
@RunWith(RobolectricTestRunner::class)
class MoviesDatabaseTest {

    private lateinit var database: MoviesDatabase
    private lateinit var local: DefaultLocalMoviesDataSource

    private var clock = 1_000L

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MoviesDatabase::class.java,
        )
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .allowMainThreadQueries()
            .build()
        local = DefaultLocalMoviesDataSource(database.moviesDao(), now = { clock })
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `a page that was never fetched reads as null`() = runTest {
        assertNull(local.observePage(1).first())
    }

    @Test
    fun `a page reads back in the server's order, not by id`() = runTest {
        local.replacePage(page(1, movie(30, "Third"), movie(10, "First"), movie(20, "Second")))

        val page = local.observePage(1).first()!!

        assertEquals(listOf("Third", "First", "Second"), page.movies.map { it.title })
        assertEquals(TOTAL_PAGES, page.totalPages)
    }

    @Test
    fun `an empty page reads as an empty page, not as never fetched`() = runTest {
        local.replacePage(MoviePage(page = 1, movies = emptyList(), totalPages = 1))

        assertEquals(emptyList<Movie>(), local.observePage(1).first()!!.movies)
    }

    @Test
    fun `replacing page 2 touches no page-1 row`() = runTest {
        local.replacePage(page(1, movie(1, "A"), movie(2, "B")))
        local.replacePage(page(2, movie(3, "C")))

        local.replacePage(page(2, movie(4, "D")))

        assertEquals(listOf("A", "B"), local.observePage(1).first()!!.movies.map { it.title })
        assertEquals(listOf("D"), local.observePage(2).first()!!.movies.map { it.title })
    }

    @Test
    fun `a movie that moves pages is one row, on the new page`() = runTest {
        local.replacePage(page(1, movie(1, "A"), movie(2, "B")))
        local.replacePage(page(2, movie(3, "C")))

        // C climbed to page 1 between two fetches.
        local.replacePage(page(1, movie(3, "C"), movie(1, "A")))

        assertEquals(listOf("C", "A"), local.observePage(1).first()!!.movies.map { it.title })
        assertEquals(emptyList<Movie>(), local.observePage(2).first()!!.movies)
    }

    @Test
    fun `the refresh leaves exactly page 1`() = runTest {
        local.replacePage(page(1, movie(1, "A")))
        local.replacePage(page(2, movie(2, "B")))
        local.replacePage(page(3, movie(3, "C")))

        local.replaceAll(page(1, movie(9, "Z")))

        assertEquals(listOf("Z"), local.observePage(1).first()!!.movies.map { it.title })
        assertNull("page 2 is gone until scrolled to again", local.observePage(2).first())
        assertNull(local.observePage(3).first())
        assertEquals(1, database.moviesDao().allMovies().size)
    }

    @Test
    fun `a detail round-trips with its row`() = runTest {
        local.replacePage(page(1, movie(1, "A")))
        val detail = MovieDetail(
            movie = movie(1, "A"),
            runtime = 139.minutes,
            tagline = "Mischief.",
            genres = listOf("Drama", "Thriller"),
            backdropUrl = "https://image.test/backdrop.jpg",
        )

        local.storeDetail(detail)

        assertEquals(detail, local.getDetail(1))
    }

    @Test
    fun `a detail for a movie no page listed still opens`() = runTest {
        val detail =
            MovieDetail(movie(7, "Deep link"), runtime = null, tagline = null, genres = emptyList(), backdropUrl = null)

        local.storeDetail(detail)

        assertEquals(detail, local.getDetail(7))
        assertNull("page 0 is never a page the list observes", local.observePage(0).first())
    }

    @Test
    fun `a movie without a detail has no detail`() = runTest {
        local.replacePage(page(1, movie(1, "A")))

        assertNull(local.getDetail(1))
    }

    private fun page(number: Int, vararg movies: Movie) = MoviePage(
        page = number,
        movies = movies.toList(),
        totalPages = TOTAL_PAGES,
    )

    private fun movie(id: Int, title: String) = Movie(
        id = id,
        title = title,
        overview = "About $title.",
        posterUrl = "https://image.test/$id.jpg",
        releaseDate = LocalDate.of(2000, 1, 1),
        rating = 7.5,
    )

    private companion object {

        const val TOTAL_PAGES = 3
    }
}
