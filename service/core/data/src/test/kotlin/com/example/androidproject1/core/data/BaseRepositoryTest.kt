package com.example.androidproject1.core.data

import com.example.androidproject1.core.domain.DataResult
import com.example.androidproject1.core.domain.exception.InternalErrorException
import com.example.androidproject1.core.domain.exception.NotFoundException
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

        suspend fun <T> call(block: suspend () -> T): DataResult<T> = repositoryCall(block)

        fun <T> flowCall(
            source: Flow<T>,
            retries: Long = 0,
        ): Flow<DataResult<T>> =
            flowRepositoryCall(source, retries = retries, retryDelayMillis = 0)
    }

    private val repository = TestRepository()

    // --- repositoryCall ---

    @Test
    fun `repositoryCall wraps a returned value in Success`() = runTest {
        assertEquals(DataResult.Success(1), repository.call { 1 })
    }

    @Test
    fun `repositoryCall wraps an unexpected throwable as an internal error`() = runTest {
        val result = repository.call { throw IllegalStateException("boom") }
        val exception = (result as DataResult.Error).exception
        assertTrue(exception is InternalErrorException)
        assertTrue(exception.cause is IllegalStateException)
    }

    @Test
    fun `repositoryCall keeps a thrown domain exception as it is`() = runTest {
        val thrown = NotFoundException(message = "gone")
        val result = repository.call { throw thrown }
        assertSame(thrown, (result as DataResult.Error).exception)
    }

    @Test
    fun `repositoryCall lets cancellation propagate`() = runTest {
        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.call { throw CancellationException("cancelled") }
            }
        }
    }

    // --- flowRepositoryCall ---

    @Test
    fun `flowRepositoryCall maps emissions to Success`() = runTest {
        assertEquals(
            listOf(DataResult.Success(1), DataResult.Success(2)),
            repository.flowCall(flowOf(1, 2)).toList(),
        )
    }

    @Test
    fun `flowRepositoryCall ends with an Error when the source throws`() = runTest {
        val source = flow {
            emit(1)
            throw NotFoundException(message = "gone")
        }
        val emissions = repository.flowCall(source).toList()
        assertEquals(DataResult.Success(1), emissions.first())
        assertTrue((emissions.last() as DataResult.Error).exception is NotFoundException)
    }

    /**
     * Regression: a flow that has thrown cannot be resumed, only resubscribed. Without retries a
     * single transient failure permanently stops a long-lived flow — which is how session
     * observation used to die for the rest of the process.
     */
    @Test
    fun `flowRepositoryCall resubscribes to the source while retries remain`() = runTest {
        var attempts = 0
        val source = flow {
            attempts++
            if (attempts < 3) throw NotFoundException(message = "transient")
            emit("recovered")
        }

        val emissions = repository.flowCall(source, retries = 5).toList()

        assertEquals(3, attempts)
        assertEquals(listOf(DataResult.Success("recovered")), emissions)
    }

    @Test
    fun `flowRepositoryCall gives up once retries are exhausted`() = runTest {
        var attempts = 0
        val source = flow<String> {
            attempts++
            throw NotFoundException(message = "permanent")
        }

        val emissions = repository.flowCall(source, retries = 2).toList()

        assertEquals(3, attempts) // the original attempt plus two retries
        assertTrue((emissions.single() as DataResult.Error).exception is NotFoundException)
    }
}
