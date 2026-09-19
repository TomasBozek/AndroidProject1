package com.example.androidproject1.feature.movies.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.feature.movies.data.database.MoviesDatabase
import com.example.androidproject1.feature.movies.data.source.DefaultLocalMoviesDataSource
import com.example.androidproject1.feature.movies.data.source.DefaultRemoteMoviesDataSource
import com.example.androidproject1.feature.movies.domain.TmdbConfig
import com.example.androidproject1.service.core.domain.error.ServerError
import com.example.androidproject1.service.core.domain.result.Outcome
import com.example.androidproject1.service.core.domain.test.FakeLogger
import com.example.androidproject1.service.core.domain.test.TestDispatchers
import com.example.androidproject1.service.network.HttpClientFactory
import com.example.androidproject1.service.network.NetworkConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

/**
 * A page is a cache entry (D79), end to end: a real `MockEngine` on one side answering TMDB's
 * shape, a real SQLite on the other.
 *
 * Faking either would leave the interesting part — that the page the network returns is the page
 * the cache reads back, and that a refresh takes the other pages with it — asserted by nothing.
 */
@RunWith(RobolectricTestRunner::class)
class DefaultMoviesRepositoryTest {

    private lateinit var database: MoviesDatabase

    /** Every request the engine saw, so a test can say "and the network was not asked". */
    private val requests = mutableListOf<String>()

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
    }

    @After
    fun tearDown() = database.close()

    private fun repository(engine: MockEngine) = DefaultMoviesRepository(
        logger = FakeLogger(),
        localMoviesDataSource = DefaultLocalMoviesDataSource(database.moviesDao()),
        remoteMoviesDataSource = DefaultRemoteMoviesDataSource(
            client = HttpClientFactory.create(
                engine = engine,
                // The template's own host: a TMDB request must leave it alone (D80).
                config = NetworkConfig(baseUrl = "https://fixtures.test/"),
            ),
            dispatcherProvider = TestDispatchers,
            config = CONFIG,
        ),
    )

    /** Answers `movie/popular` by page and `movie/<id>` from the same rows; anything else 404s. */
    private fun tmdb(pages: Int = 3, pageSize: Int = 2) = MockEngine { request ->
        requests += request.url.toString()
        val url = request.url
        assertEquals("api.themoviedb.test", url.host)
        assertEquals("the key rides as api_key", CONFIG.apiKey, url.parameters["api_key"])
        val route = url.segments.dropWhile { it == "3" }
        when {
            route == listOf("movie", "popular") -> {
                val page = url.parameters["page"]!!.toInt()
                if (page > pages) {
                    respondError(HttpStatusCode.UnprocessableEntity)
                } else {
                    val rows = (1..pageSize).map { row(id = (page - 1) * pageSize + it, page = page) }
                    val results = rows.joinToString(",")
                    val total = pages * pageSize
                    respondJson("""{"page":$page,"results":[$results],"total_pages":$pages,"total_results":$total}""")
                }
            }

            route.size == 2 && route[0] == "movie" -> {
                val id = route[1].toInt()
                if (id > pages * pageSize) respondError(HttpStatusCode.NotFound) else respondJson(detail(id))
            }

            else -> respondError(HttpStatusCode.NotFound)
        }
    }

    private fun failing() = MockEngine {
        requests += it.url.toString()
        respondError(HttpStatusCode.ServiceUnavailable)
    }

    private fun MockRequestHandleScope.respondJson(json: String) = respond(
        content = json,
        status = HttpStatusCode.OK,
        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
    )

    @Test
    fun `a cache miss fetches the page and returns what the server sent`() = runTest {
        val outcome = repository(tmdb()).observePage(2).first()

        val page = (outcome as Outcome.Success).data
        assertEquals(2, page.page)
        assertEquals(listOf("Movie 3", "Movie 4"), page.movies.map { it.title })
        assertEquals(3, page.totalPages)
        // The mapper's work: a path became a URL, a date a date, nothing else leaked.
        assertEquals("https://image.test/w342/3.jpg", page.movies[0].posterUrl)
        assertEquals(LocalDate.of(2001, 1, 3), page.movies[0].releaseDate)
    }

    @Test
    fun `what the server sent is written to the cache`() = runTest {
        repository(tmdb()).observePage(1).first()

        assertEquals(2, database.moviesDao().observePage(1).first().size)
    }

    @Test
    fun `a cached page is emitted before the network answers`() = runTest {
        repository(tmdb()).observePage(1).first()
        requests.clear()

        val emissions = repository(tmdb()).observePage(1).take(1).toList()

        assertEquals(listOf("Movie 1", "Movie 2"), (emissions[0] as Outcome.Success).data.movies.map { it.title })
        assertTrue("the first emission is the table's, not the network's", requests.isEmpty())
    }

    @Test
    fun `a failure on page 2 emits the failure and leaves page 1 in the table`() = runTest {
        repository(tmdb()).observePage(1).first()

        val emissions = repository(failing()).observePage(2).take(1).toList()

        assertTrue((emissions[0] as Outcome.Failure).error is ServerError)
        assertEquals(2, database.moviesDao().observePage(1).first().size)
        assertNull("page 2 was never written", database.moviesDao().observePageMarker(2).first())
    }

    @Test
    fun `a failure over a cached page emits the page and then the failure`() = runTest {
        repository(tmdb()).observePage(1).first()

        val emissions = repository(failing()).observePage(1).toList()

        assertEquals(2, emissions.size)
        assertEquals(2, (emissions[0] as Outcome.Success).data.movies.size)
        assertTrue((emissions[1] as Outcome.Failure).error is ServerError)
    }

    @Test
    fun `a refresh on a good remote leaves exactly page 1`() = runTest {
        val repository = repository(tmdb())
        repository.observePage(1).first()
        repository.observePage(2).first()
        repository.observePage(3).first()

        val outcome = repository.refresh()

        assertTrue(outcome is Outcome.Success)
        assertEquals(2, database.moviesDao().allMovies().size)
        assertNull(database.moviesDao().observePageMarker(2).first())
        assertNull(database.moviesDao().observePageMarker(3).first())
    }

    @Test
    fun `a refresh on a failing remote returns the failure and drops nothing`() = runTest {
        repository(tmdb()).observePage(1).first()
        repository(tmdb()).observePage(2).first()

        val outcome = repository(failing()).refresh()

        assertTrue((outcome as Outcome.Failure).error is ServerError)
        assertEquals(4, database.moviesDao().allMovies().size)
    }

    @Test
    fun `a movie is fetched once on a cold cache and never on a warm one`() = runTest {
        val repository = repository(tmdb())

        val first = repository.getMovie(3)
        requests.clear()
        val second = repository.getMovie(3)

        val detail = (first as Outcome.Success).data
        assertEquals("Movie 3", detail.movie.title)
        assertEquals(100.minutes, detail.runtime)
        assertEquals(listOf("Drama", "Thriller"), detail.genres)
        assertEquals(detail, (second as Outcome.Success).data)
        assertTrue("the second read was the table's", requests.isEmpty())
    }

    @Test
    fun `a movie the server does not have is a failure, not a row`() = runTest {
        val outcome = repository(tmdb()).getMovie(99)

        assertTrue(outcome is Outcome.Failure)
        assertNull(database.moviesDao().movie(99))
    }

    private fun row(id: Int, page: Int) =
        """{"id":$id,"title":"Movie $id","overview":"About $id.","poster_path":"/$id.jpg",""" +
            """"release_date":"2001-01-0$id","vote_average":7.$page,"genre_ids":[18,53]}"""

    private fun detail(id: Int) =
        """{"id":$id,"title":"Movie $id","overview":"About $id.","poster_path":"/$id.jpg",""" +
            """"release_date":"2001-01-0$id","vote_average":7.0,"runtime":100,"tagline":"Tag $id.",""" +
            """"genres":[{"id":18,"name":"Drama"},{"id":53,"name":"Thriller"}],"backdrop_path":null}"""

    private companion object {

        val CONFIG = TmdbConfig(
            apiBaseUrl = "https://api.themoviedb.test/3/",
            imageBaseUrl = "https://image.test/w342",
            apiKey = "k",
        )
    }
}
