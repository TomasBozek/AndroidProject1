package com.example.androidproject1.network

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The `dev` fixture engine, driven through a real client: the TMDB host's routes (D80) and the
 * one switch that turns every route into a `503`.
 *
 * The fixtures are read off the `dev` resources, which is why this runs under Robolectric — the
 * engine wants a `Context`, and a test that pasted the JSON in would pass on a fixture the app
 * cannot load. A plain [Application], not [com.example.androidproject1.App]: the engine needs the
 * resources, not the Koin graph, and a graph started per test is a graph started twice.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class NetworkEngineTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val client = HttpClient(networkEngine(context, cacheSizeBytes = 0))

    @After
    fun tearDown() {
        FixtureNetwork.failingOverride = false
        client.close()
    }

    @Test
    fun `a popular page is the fixture for that page`() = runBlocking {
        val response = client.get("$TMDB/movie/popular?page=2&api_key=test")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(2, body.getValue("page").jsonPrimitive.content.toInt())
        assertEquals(PAGE_SIZE, body.getValue("results").jsonArray.size)
        assertEquals(PAGES, body.getValue("total_pages").jsonPrimitive.content.toInt())
    }

    @Test
    fun `no page means the first`() = runBlocking {
        val body = Json.parseToJsonElement(client.get("$TMDB/movie/popular").bodyAsText()).jsonObject

        assertEquals(1, body.getValue("page").jsonPrimitive.content.toInt())
    }

    @Test
    fun `a page past the last answers 422 the way TMDB does`() = runBlocking {
        val response = client.get("$TMDB/movie/popular?page=${PAGES + 1}")

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("false", body.getValue("success").jsonPrimitive.content)
    }

    @Test
    fun `a movie is its list row merged with its detail`() = runBlocking {
        val page = Json.parseToJsonElement(client.get("$TMDB/movie/popular?page=1").bodyAsText()).jsonObject
        val first = page.getValue("results").jsonArray.first().jsonObject
        val id = first.getValue("id").jsonPrimitive.content

        val response = client.get("$TMDB/movie/$id")

        assertEquals(HttpStatusCode.OK, response.status)
        val movie = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(first.getValue("title"), movie.getValue("title"))
        assertTrue("the detail carries a runtime", movie.getValue("runtime").jsonPrimitive.content.toInt() > 0)
        assertTrue("the detail spells its genres out", movie.getValue("genres").jsonArray.isNotEmpty())
        assertNull("the detail has no genre_ids, like TMDB's", movie["genre_ids"])
    }

    @Test
    fun `every row of every page has a detail`() = runBlocking {
        val ids = (1..PAGES).flatMap { page ->
            Json.parseToJsonElement(client.get("$TMDB/movie/popular?page=$page").bodyAsText())
                .jsonObject.getValue("results").jsonArray.map { it.jsonObject.getValue("id").jsonPrimitive.content }
        }

        assertEquals("sixty distinct ids", PAGES * PAGE_SIZE, ids.toSet().size)
        ids.forEach { id ->
            val movie = Json.parseToJsonElement(client.get("$TMDB/movie/$id").bodyAsText()).jsonObject
            assertFalse("movie $id has no tagline", movie["tagline"]?.jsonPrimitive?.content.isNullOrBlank())
        }
    }

    @Test
    fun `an unknown movie is a 404`() = runBlocking {
        assertEquals(HttpStatusCode.NotFound, client.get("$TMDB/movie/1").status)
    }

    @Test
    fun `the catalog host keeps its own routes`() = runBlocking {
        val response = client.get("https://dev.example.com/categories")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(Json.parseToJsonElement(response.bodyAsText()).jsonArray.isNotEmpty())
    }

    @Test
    fun `the failing switch breaks every host`() = runBlocking {
        FixtureNetwork.failingOverride = true

        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("$TMDB/movie/popular").status)
        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("https://dev.example.com/categories").status)
    }

    private companion object {

        const val TMDB = "https://api.themoviedb.org/3"
        const val PAGES = 3
        const val PAGE_SIZE = 20
    }
}
