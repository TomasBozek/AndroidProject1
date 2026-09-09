package com.example.androidproject1.core.network

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SingleFlightTokenRefresherTest {

    private val refreshCalls = AtomicInteger(0)

    private fun refresher(
        store: FakeTokenStore = FakeTokenStore(),
        result: (String) -> Tokens? = { Tokens("fresh-access", "refresh-2") },
    ): Pair<SingleFlightTokenRefresher, FakeTokenStore> {
        val tokenRefresher = object : TokenRefresher {
            override suspend fun refresh(refreshToken: String): Tokens? {
                refreshCalls.incrementAndGet()
                // Long enough that a second caller is certainly waiting on the mutex.
                delay(50)
                return result(refreshToken)
            }
        }
        return SingleFlightTokenRefresher(store, tokenRefresher) to store
    }

    @Test
    fun `a refresh stores the new tokens`() = runTest {
        val (subject, store) = refresher()

        val tokens = subject.refresh(staleAccessToken = "stale-access")

        assertEquals("fresh-access", tokens?.accessToken)
        assertEquals("fresh-access", store.accessToken())
        assertEquals("refresh-2", store.refreshToken())
    }

    @Test
    fun `two parallel 401s cause one refresh`() = runTest {
        val (subject, _) = refresher()

        val results = listOf(
            async { subject.refresh(staleAccessToken = "stale-access") },
            async { subject.refresh(staleAccessToken = "stale-access") },
        ).awaitAll()

        // The second caller waited on the mutex, saw the access token had changed, and took it.
        assertEquals(1, refreshCalls.get())
        assertEquals(listOf("fresh-access", "fresh-access"), results.map { it?.accessToken })
    }

    @Test
    fun `five parallel 401s still cause one refresh`() = runTest {
        val (subject, _) = refresher()

        List(5) { async { subject.refresh(staleAccessToken = "stale-access") } }.awaitAll()

        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `a rejected refresh token clears the session`() = runTest {
        val (subject, store) = refresher(result = { null })

        val tokens = subject.refresh(staleAccessToken = "stale-access")

        assertNull(tokens)
        assertTrue(store.cleared)
        assertNull(store.accessToken())
    }

    @Test
    fun `no refresh token at all clears the session without calling the server`() = runTest {
        val (subject, store) = refresher(store = FakeTokenStore(refresh = null))

        val tokens = subject.refresh(staleAccessToken = "stale-access")

        assertNull(tokens)
        assertEquals(0, refreshCalls.get())
        assertTrue(store.cleared)
    }

    @Test
    fun `a caller arriving after a refresh takes the stored token`() = runTest {
        val (subject, _) = refresher()
        subject.refresh(staleAccessToken = "stale-access")

        // Its request failed on the old token, but the store has already moved on.
        val tokens = subject.refresh(staleAccessToken = "stale-access")

        assertEquals(1, refreshCalls.get())
        assertEquals("fresh-access", tokens?.accessToken)
    }
}
