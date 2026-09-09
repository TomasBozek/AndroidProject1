package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.error.NotFoundError
import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.result.Outcome
import com.example.androidproject1.core.domain.test.FakeLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class BaseRepositoryTest {

    /** Exposes [BaseRepository]'s protected helpers so they can be exercised directly. */
    private class TestRepository : BaseRepository(FakeLogger()) {

        suspend fun <T> call(block: suspend () -> T): Outcome<T> = execute(block)

        fun <T> flowCall(
            source: Flow<T>,
            retries: Long = 0,
        ): Flow<Outcome<T>> =
            observe(source, retries = retries, retryDelayMillis = 0)

        fun <T : Any> cachedCall(
            local: Flow<T?>,
            remote: suspend () -> T,
            write: suspend (T) -> Unit,
        ): Flow<Outcome<T>> = cached(local, remote, write)
    }

    private val repository = TestRepository()

    // --- execute ---

    @Test
    fun `execute wraps a returned value in Success`() = runTest {
        assertEquals(Outcome.Success(1), repository.call { 1 })
    }

    @Test
    fun `execute wraps an unexpected throwable as an unexpected error`() = runTest {
        val result = repository.call { throw IllegalStateException("boom") }
        val error = (result as Outcome.Failure).error
        assertTrue(error is UnexpectedError)
        assertTrue(error.cause is IllegalStateException)
    }

    @Test
    fun `execute keeps a thrown domain error as it is`() = runTest {
        val thrown = NotFoundError(message = "gone")
        val result = repository.call { throw thrown }
        assertSame(thrown, (result as Outcome.Failure).error)
    }

    @Test
    fun `execute lets cancellation propagate`() = runTest {
        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.call { throw CancellationException("cancelled") }
            }
        }
    }

    // --- observe ---

    @Test
    fun `observe maps emissions to Success`() = runTest {
        assertEquals(
            listOf(Outcome.Success(1), Outcome.Success(2)),
            repository.flowCall(flowOf(1, 2)).toList(),
        )
    }

    @Test
    fun `observe ends with a Failure when the source throws`() = runTest {
        val source = flow {
            emit(1)
            throw NotFoundError(message = "gone")
        }
        val emissions = repository.flowCall(source).toList()
        assertEquals(Outcome.Success(1), emissions.first())
        assertTrue((emissions.last() as Outcome.Failure).error is NotFoundError)
    }

    /**
     * Regression: a flow that has thrown cannot be resumed, only resubscribed. Without retries a
     * single transient failure permanently stops a long-lived flow — which is how session
     * observation used to die for the rest of the process.
     */
    @Test
    fun `observe resubscribes to the source while retries remain`() = runTest {
        var attempts = 0
        val source = flow {
            attempts++
            if (attempts < 3) throw NotFoundError(message = "transient")
            emit("recovered")
        }

        val emissions = repository.flowCall(source, retries = 5).toList()

        assertEquals(3, attempts)
        assertEquals(listOf(Outcome.Success("recovered")), emissions)
    }

    @Test
    fun `observe gives up once retries are exhausted`() = runTest {
        var attempts = 0
        val source = flow<String> {
            attempts++
            throw NotFoundError(message = "permanent")
        }

        val emissions = repository.flowCall(source, retries = 2).toList()

        assertEquals(3, attempts) // the original attempt plus two retries
        assertTrue((emissions.single() as Outcome.Failure).error is NotFoundError)
    }

    // --- cached ---

    /**
     * A cache that behaves like a store: `write` puts a value in and the flow emits it.
     * `MutableStateFlow` rather than a list, because the ordering assertions below are about what
     * a real collector would see.
     */
    private class FakeCache<T : Any>(initial: T? = null) {

        val state = MutableStateFlow(initial)

        var writes = 0
            private set

        fun write(value: T) {
            writes++
            state.value = value
        }
    }

    @Test
    fun `cached with an empty cache emits only what the network returned`() = runTest {
        val cache = FakeCache<String>()

        val emissions = repository.cachedCall(
            local = cache.state,
            remote = { "fresh" },
            write = { cache.write(it) },
        ).take(1).toList()

        assertEquals(listOf(Outcome.Success("fresh")), emissions)
        assertEquals(1, cache.writes)
    }

    @Test
    fun `cached emits the cache first, then the refreshed value`() = runTest {
        val cache = FakeCache("stale")

        val emissions = repository.cachedCall(
            local = cache.state,
            remote = { "fresh" },
            write = { cache.write(it) },
        ).take(2).toList()

        // The order is the point: a screen draws the stale value immediately and swaps it.
        assertEquals(listOf(Outcome.Success("stale"), Outcome.Success("fresh")), emissions)
    }

    @Test
    fun `cached over a stale cache emits the data and then the failure`() = runTest {
        val cache = FakeCache("stale")

        val emissions = repository.cachedCall<String>(
            local = cache.state,
            remote = { throw NotFoundError(message = "gone") },
            write = { cache.write(it) },
        ).toList()

        // Both, in that order: the screen keeps showing "stale" and puts the error beside it
        // rather than replacing a working screen. Nothing was written, so nothing follows.
        assertEquals(2, emissions.size)
        assertEquals(Outcome.Success("stale"), emissions[0])
        assertTrue((emissions[1] as Outcome.Failure).error is NotFoundError)
        assertEquals(0, cache.writes)
    }

    @Test
    fun `cached with no cache and a failing network emits only the failure`() = runTest {
        val cache = FakeCache<String>()

        val emissions = repository.cachedCall<String>(
            local = cache.state,
            remote = { throw NotFoundError(message = "gone") },
            write = { cache.write(it) },
        ).take(1).toList()

        // No stale data to show, so the screen has an error state and nothing behind it.
        assertEquals(1, emissions.size)
        assertTrue((emissions[0] as Outcome.Failure).error is NotFoundError)
    }

    @Test
    fun `cached keeps emitting later cache changes`() = runTest {
        val cache = FakeCache("stale")
        val emissions = mutableListOf<Outcome<String>>()

        val job = launch {
            repository.cachedCall(
                local = cache.state,
                remote = { "fresh" },
                write = { cache.write(it) },
            ).collect { emissions.add(it) }
        }
        runCurrent()
        // Something else wrote to the same cache — another screen, a sync.
        cache.state.value = "later"
        runCurrent()
        job.cancel()

        assertEquals(
            listOf(Outcome.Success("stale"), Outcome.Success("fresh"), Outcome.Success("later")),
            emissions,
        )
    }

    @Test
    fun `cached does not redraw when the cache re-emits the same value`() = runTest {
        val cache = FakeCache("same")
        val emissions = mutableListOf<Outcome<String>>()

        val job = launch {
            repository.cachedCall(
                local = cache.state,
                remote = { "same" },
                write = { cache.write(it) },
            ).collect { emissions.add(it) }
        }
        runCurrent()
        job.cancel()

        // The refresh wrote the value the cache already held, so there is nothing new to show.
        assertEquals(listOf(Outcome.Success("same")), emissions)
    }

    @Test
    fun `cached maps an unexpected throwable like every other path`() = runTest {
        val cache = FakeCache<String>()

        val emissions = repository.cachedCall<String>(
            local = cache.state,
            remote = { throw IllegalStateException("boom") },
            write = { cache.write(it) },
        ).take(1).toList()

        assertTrue((emissions[0] as Outcome.Failure).error is UnexpectedError)
    }
}
