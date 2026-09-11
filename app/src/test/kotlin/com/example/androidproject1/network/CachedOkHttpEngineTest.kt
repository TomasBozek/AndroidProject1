package com.example.androidproject1.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/** A real request/response cycle against a real cache — the behaviour worth trusting a fake for. */
class CachedOkHttpEngineTest {

    private val server = MockWebServer()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `a second identical GET is served from the cache, not the network`() {
        server.enqueue(MockResponse().setBody("{}").setHeader("Cache-Control", "max-age=60"))

        val client = HttpClient(
            cachedOkHttpEngine(cacheDirectory = createTempCacheDir(), cacheSizeBytes = CACHE_SIZE_BYTES),
        )

        runBlocking {
            client.get(server.url("/thing").toString())
            client.get(server.url("/thing").toString())
        }

        // One request enqueued and one taken: a second call reaching the network would have
        // nothing left to answer with.
        assertEquals(1, server.requestCount)

        client.close()
    }

    private fun createTempCacheDir(): File = File.createTempFile("http-cache-test", null).let {
        it.delete()
        it.mkdirs()
        it
    }

    private companion object {

        const val CACHE_SIZE_BYTES = 1L * 1024 * 1024
    }
}
