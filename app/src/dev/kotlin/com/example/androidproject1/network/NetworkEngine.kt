package com.example.androidproject1.network

import android.content.Context
import com.example.androidproject1.R
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
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
    FixtureNetwork.attach(context)

    return MockEngine { request ->
        if (FixtureNetwork.failing) {
            // 503 rather than a thrown IOException: it exercises the status table as well as the
            // stale-cache path, and it is what a real server does when it is having a bad day.
            return@MockEngine respondError(HttpStatusCode.ServiceUnavailable)
        }

        val path = request.url.encodedPath.trimEnd('/').substringAfterLast('/')
        val body = when (path) {
            "categories" -> categories
            "products" -> products.forCategory(request.url.parameters["categoryId"])
            "product" -> products.forProduct(request.url.parameters["id"])
                ?: return@MockEngine respondError(HttpStatusCode.NotFound)

            else -> return@MockEngine respondError(HttpStatusCode.NotFound)
        }
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
        )
    }
}

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
 * adb are the same thing.
 */
object FixtureNetwork {

    private const val MARKER = "fail_network"

    private var filesDir: java.io.File? = null

    /** Set from a var too, so a test can flip it without touching the filesystem. */
    @Volatile
    var failingOverride: Boolean = false

    val failing: Boolean
        get() = failingOverride || filesDir?.let { java.io.File(it, MARKER).exists() } == true

    internal fun attach(context: Context) {
        filesDir = context.filesDir
    }

    /**
     * Writes the marker, so the choice survives the process it was made in — which is what a
     * tester expects of a switch that says "the server is down".
     */
    fun setFailing(context: Context, failing: Boolean) {
        val marker = java.io.File(context.filesDir, MARKER)
        if (failing) marker.createNewFile() else marker.delete()
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
