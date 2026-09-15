package com.example.androidproject1.service.network

import com.example.androidproject1.service.core.domain.error.ServerError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * The retry policy, driven through the real client on `MockEngine`.
 *
 * Every test counts requests rather than inspecting the plugin: how many times the server hears
 * about a call is the thing a double order is made of.
 */
class HttpClientRetryTest {

    private val config = NetworkConfig(baseUrl = "https://example.test/")

    private var requests = 0

    /** What the plugin asked to wait, without waiting it — `delay` exists to be replaced in a test. */
    private val delays = mutableListOf<Long>()

    /**
     * A client whose engine answers by attempt number, counting requests as it goes.
     *
     * The retry block is re-opened rather than re-installed: Ktor composes plugin configuration,
     * so this replaces the delay function and leaves the factory's policy exactly as it ships.
     */
    private fun clientFor(
        config: NetworkConfig = this.config,
        connectivity: ConnectivityMonitor = ConnectivityMonitor.AlwaysOnline,
        answer: MockRequestHandleScope.(attempt: Int) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine {
            requests += 1
            answer(requests)
        }
        return HttpClientFactory.create(engine, config, connectivity = connectivity).config {
            install(HttpRequestRetry) { delay { delays += it } }
        }
    }

    @Test
    fun `a GET that fails twice succeeds on the third request`() = runTest {
        val client = clientFor { attempt ->
            if (attempt <= 2) respondError(HttpStatusCode.ServiceUnavailable) else respond("ok")
        }

        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(3, requests)
    }

    @Test
    fun `a GET that never recovers is tried three times and then fails`() = runTest {
        val client = clientFor { respondError(HttpStatusCode.InternalServerError) }

        val thrown = runCatching { client.get("/products") }.exceptionOrNull()

        // The first try plus the two retries the default configures — and then the failure the
        // caller was always going to see, mapped as if there had been no retry at all.
        assertEquals(3, requests)
        assertTrue(HttpErrorMapper.map(requireNotNull(thrown)) is ServerError)
    }

    @Test
    fun `a POST is never retried, because a retried order is a double order`() = runTest {
        val client = clientFor { respondError(HttpStatusCode.ServiceUnavailable) }

        runCatching { client.post("/orders") }

        assertEquals(1, requests)
    }

    @Test
    fun `a transport failure on a GET is retried`() = runTest {
        val client = clientFor { attempt ->
            if (attempt == 1) throw IOException("no route to host") else respond("ok")
        }

        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, requests)
    }

    @Test
    fun `a transport failure on a POST is not`() = runTest {
        val client = clientFor { throw IOException("no route to host") }

        runCatching { client.post("/orders") }

        assertEquals(1, requests)
    }

    @Test
    fun `a transport failure while offline is not retried`() = runTest {
        // Three tries on a device with no route are three identical failures and two seconds of
        // backoff, in front of an answer — you are offline — that was known before the first.
        val client = clientFor(connectivity = Offline) { throw IOException("no route to host") }

        runCatching { client.get("/products") }

        assertEquals(1, requests)
        assertTrue(delays.isEmpty())
    }

    @Test
    fun `the backoff grows, and carries jitter`() = runTest {
        val client = clientFor { respondError(HttpStatusCode.ServiceUnavailable) }

        runCatching { client.get("/products") }

        // 500 ms then 1 s, each plus up to 250 ms of jitter: exponential, bounded, never zero.
        assertEquals(2, delays.size)
        assertTrue("first delay was ${delays[0]}", delays[0] in 500..750)
        assertTrue("second delay was ${delays[1]}", delays[1] in 1000..1250)
    }

    @Test
    fun `retries = 0 installs no retry at all`() = runTest {
        // Built without the test's delay override, so nothing here can install the plugin but the
        // factory — which is the thing being asserted.
        val engine = MockEngine {
            requests += 1
            respondError(HttpStatusCode.ServiceUnavailable)
        }
        val client = HttpClientFactory.create(engine, config.copy(retries = 0))

        runCatching { client.get("/products") }

        assertEquals(1, requests)
    }
}

private object Offline : ConnectivityMonitor {

    override val online: StateFlow<Boolean> = MutableStateFlow(false)
}
