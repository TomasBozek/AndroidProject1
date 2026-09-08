package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.error.NotFoundError
import com.example.androidproject1.core.domain.error.UnexpectedError
import com.example.androidproject1.core.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
}
