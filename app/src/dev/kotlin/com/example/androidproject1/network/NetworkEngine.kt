package com.example.androidproject1.network

import android.content.Context
import com.example.androidproject1.R
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.service.network.ConnectivityMonitor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The `dev` flavor talks to fixtures, not a server (D20).
 *
 * A `MockEngine` rather than a bundled server: it needs nothing running, CI gets the same
 * behaviour as a laptop, and the fixtures are reviewable JSON in `res/raw`. What it cannot do is
 * go offline on its own — the engine is in-process — which is what [FixtureNetwork.failing] is
 * for: flip it and every request fails, so the cache-then-network path can be seen by hand.
 *
 * @param cacheSizeBytes unused — a fixture is not worth caching, and this parameter exists only
 * so the one call site in `ApplicationModule` compiles against every flavor's `networkEngine`.
 */
fun networkEngine(context: Context, cacheSizeBytes: Long): HttpClientEngine {
    val categories = context.readRaw(R.raw.fixture_categories)
    val products = context.readRaw(R.raw.fixture_products)
    val movies = TmdbFixtures(context)
    FixtureNetwork.attach(context)

    return MockEngine { request ->
        if (FixtureNetwork.failing) {
            // 503 rather than a thrown IOException: it exercises the status table as well as the
            // stale-cache path, and it is what a real server does when it is having a bad day.
            return@MockEngine respondError(HttpStatusCode.ServiceUnavailable)
        }

        // The host first: TMDB is a second server with paths of its own (D80), and `movie/550`
        // and `product?id=` must not be told apart by their last segment alone.
        if (request.url.host == TMDB_HOST) return@MockEngine movies.handle(this, request.url)

        val path = request.url.encodedPath.trimEnd('/').substringAfterLast('/')
        val body = when (path) {
            "categories" -> categories
            "products" -> products.forCategory(request.url.parameters["categoryId"])
            "product" -> products.forProduct(request.url.parameters["id"])
                ?: return@MockEngine respondError(HttpStatusCode.NotFound)

            else -> return@MockEngine respondError(HttpStatusCode.NotFound)
        }
        respondJson(body)
    }
}

private const val TMDB_HOST = "api.themoviedb.org"

private fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK) = respond(
    content = body,
    status = status,
    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
)

/**
 * What the fixture engine answers under [TMDB_HOST]: `movie/popular?page=n` from three page
 * fixtures in TMDB's own shape, and `movie/<id>` as the list row merged with its detail — the
 * fields only the detail endpoint carries live in one file keyed by id, so a movie is described
 * once and both endpoints agree about it.
 *
 * A page past the last one answers `422` with TMDB's own body, because that is what the real
 * host does and the client's end-of-list handling should be exercised on the same status.
 */
private class TmdbFixtures(context: Context) {

    private val pages: List<JsonObject> = listOf(
        R.raw.fixture_movies_page_1,
        R.raw.fixture_movies_page_2,
        R.raw.fixture_movies_page_3,
    ).map { Json.parseToJsonElement(context.readRaw(it)).jsonObject }

    private val details: Map<String, JsonObject> = Json.parseToJsonElement(context.readRaw(R.raw.fixture_movie_details))
        .jsonArray.associateBy { it.jsonObject.getValue("id").jsonPrimitive.content }.mapValues { it.value.jsonObject }

    fun handle(scope: MockRequestHandleScope, url: Url): HttpResponseData = with(scope) {
        val segments = url.segments.filter { it.isNotEmpty() }
        // `/3/movie/popular` → `movie/popular`; the version prefix is TMDB's, not a route.
        val route = segments.dropWhile { it == "3" }
        when {
            route == listOf("movie", "popular") -> {
                val page = url.parameters["page"]?.toIntOrNull() ?: 1
                pages.getOrNull(page - 1)?.let { respondJson(it.toString()) }
                    ?: respondJson(PAGE_OUT_OF_RANGE, HttpStatusCode.UnprocessableEntity)
            }

            route.size == 2 && route[0] == "movie" -> movie(route[1])?.let { respondJson(it) }
                ?: respondJson(NOT_FOUND, HttpStatusCode.NotFound)

            else -> respondJson(NOT_FOUND, HttpStatusCode.NotFound)
        }
    }

    private fun movie(id: String): String? {
        val row = pages.asSequence()
            .flatMap { it.getValue("results").jsonArray }
            .firstOrNull { it.jsonObject.getValue("id").jsonPrimitive.content == id }
            ?.jsonObject ?: return null
        val detail = details[id].orEmpty()
        // The detail endpoint spells genres out and has no `genre_ids`; mirror that.
        return JsonObject(row - "genre_ids" + detail).toString()
    }

    private companion object {

        const val PAGE_OUT_OF_RANGE = """{"success":false,"status_code":22,""" +
            """"status_message":"Invalid page: Pages start at 1 and max at 500. They are expected to be an integer."}"""
        const val NOT_FOUND =
            """{"success":false,"status_code":34,"status_message":"The resource you requested could not be found."}"""
    }
}

/**
 * The fixture engine is in-process, so airplane mode means nothing to it and the one "offline"
 * `dev` has is its own switch: the banner and the retry policy follow [FixtureNetwork] (D68).
 *
 * @param dispatchers unused — nothing here is observed from the platform; the parameter exists so
 * the one call site in `ApplicationModule` compiles against every flavor's `connectivityMonitor`.
 */
@Suppress("UNUSED_PARAMETER")
fun connectivityMonitor(context: Context, dispatchers: DispatcherProvider): ConnectivityMonitor = FixtureNetwork

/**
 * Whether the fixture engine is pretending the server is down.
 *
 * Backed by the presence of a file, not just a flag, so it can be flipped from outside a running
 * process — the engine is in-process, so there is no airplane mode to reach for:
 *
 * ```bash
 * adb shell run-as com.example.androidproject1.dev touch files/fail_network   # break it
 * adb shell run-as com.example.androidproject1.dev rm files/fail_network      # fix it
 * ```
 *
 * The debug menu's offline switch writes the same file, so flipping it there and touching it over
 * adb are the same thing — except to [online], which follows the switch and the launch: a marker
 * touched over adb is seen by the engine at once and by the banner at the next flip or start.
 */
object FixtureNetwork : ConnectivityMonitor {

    private const val MARKER = "fail_network"

    private var filesDir: java.io.File? = null

    /** Set from a var too, so a test can flip it without touching the filesystem. */
    @Volatile
    var failingOverride: Boolean = false
        set(value) {
            field = value
            publish()
        }

    val failing: Boolean
        get() = failingOverride || filesDir?.let { java.io.File(it, MARKER).exists() } == true

    private val onlineState = MutableStateFlow(true)

    /** The inverse of [failing], as the port the banner and the retry policy read. */
    override val online: StateFlow<Boolean> = onlineState.asStateFlow()

    internal fun attach(context: Context) {
        filesDir = context.filesDir
        publish()
    }

    /**
     * Writes the marker, so the choice survives the process it was made in — which is what a
     * tester expects of a switch that says "the server is down".
     */
    fun setFailing(context: Context, failing: Boolean) {
        val marker = java.io.File(context.filesDir, MARKER)
        if (failing) marker.createNewFile() else marker.delete()
        publish()
    }

    private fun publish() {
        onlineState.value = !failing
    }
}

private fun Context.readRaw(id: Int): String =
    resources.openRawResource(id).bufferedReader().use { it.readText() }

/**
 * Filters the product fixture the way the endpoint would.
 *
 * Parsed rather than string-sliced: a fixture that got its own JSON subtly wrong would fail the
 * app for a reason that has nothing to do with the app.
 */
private fun String.forCategory(categoryId: String?): String {
    if (categoryId == null) return this
    val kept = Json.parseToJsonElement(this).jsonArray.filter {
        it.jsonObject["categoryId"]?.jsonPrimitive?.content == categoryId
    }
    return JsonArray(kept).toString()
}

/**
 * One product's own JSON object, by id — `null` if the fixture has no such product, which is a
 * 404 to the caller. The full fixture list is searched regardless of what the local table holds:
 * the "server" knows about every product whether or not this session has browsed to it.
 */
private fun String.forProduct(productId: String?): String? {
    val found = Json.parseToJsonElement(this).jsonArray.firstOrNull {
        it.jsonObject["id"]?.jsonPrimitive?.content == productId
    } ?: return null
    return found.toString()
}
