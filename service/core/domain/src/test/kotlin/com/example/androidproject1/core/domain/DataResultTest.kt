package com.example.androidproject1.core.domain

import com.example.androidproject1.core.domain.exception.DomainException
import com.example.androidproject1.core.domain.exception.InternalErrorException
import com.example.androidproject1.core.domain.exception.NotFoundException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class DataResultTest {

    private fun <T> success(value: T) = DataResult.Success(value)

    private fun error(exception: DomainException = NotFoundException()) = DataResult.Error(exception)

    // --- mapSuccessData ---

    @Test
    fun `mapSuccessData transforms the success value`() {
        assertEquals(success(2), success(1).mapSuccessData { it + 1 })
    }

    @Test
    fun `mapSuccessData passes an error through untouched`() {
        val original = error()
        assertSame(original.exception, (original.mapSuccessData { it } as DataResult.Error).exception)
    }

    @Test
    fun `mapSuccessData wraps an unexpected throwable as an internal error`() {
        val result = success(1).mapSuccessData<Int, Int> { throw IllegalStateException("boom") }
        val exception = (result as DataResult.Error).exception
        assertTrue(exception is InternalErrorException)
        assertTrue(exception.cause is IllegalStateException)
    }

    @Test
    fun `mapSuccessData keeps a thrown domain exception as it is`() {
        val thrown = NotFoundException(message = "gone")
        val result = success(1).mapSuccessData<Int, Int> { throw thrown }
        assertSame(thrown, (result as DataResult.Error).exception)
    }

    /**
     * Regression: `runCatching` catches [CancellationException] too, so without an explicit rethrow
     * a cancelled coroutine would be reported to the user as an internal error.
     */
    @Test
    fun `mapSuccessData rethrows cancellation instead of reporting an error`() {
        assertThrows(CancellationException::class.java) {
            success(1).mapSuccessData<Int, Int> { throw CancellationException("cancelled") }
        }
    }

    // --- onSuccess / onError ---

    @Test
    fun `onSuccess chains into the next result`() {
        assertEquals(success("1"), success(1).onSuccess { success(it.toString()) })
    }

    @Test
    fun `onSuccess rethrows cancellation`() {
        assertThrows(CancellationException::class.java) {
            success(1).onSuccess<Int, Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `onError can recover an error into a success`() {
        assertEquals(success(0), error().onError { success(0) })
    }

    @Test
    fun `onError leaves a success alone`() {
        assertEquals(success(1), success(1).onError { success(0) })
    }

    @Test
    fun `onError rethrows cancellation`() {
        assertThrows(CancellationException::class.java) {
            error().onError<Int> { throw CancellationException("cancelled") }
        }
    }

    // --- combining ---

    @Test
    fun `combineDataResults pairs two successes`() = runTest {
        val combined = combineDataResults(flowOf(success(1)), flowOf(success("a"))).toList()
        assertEquals(listOf(success(1 to "a")), combined)
    }

    @Test
    fun `combineDataResults short-circuits on the first error`() = runTest {
        val failure = NotFoundException(message = "missing")
        val combined =
            combineDataResults(flowOf(DataResult.Error(failure)), flowOf(success("a"))).toList()
        assertSame(failure, (combined.single() as DataResult.Error).exception)
    }

    @Test
    fun `chainedDataResults feeds the first value into the second flow`() = runTest {
        val chained = chainedDataResults(flowOf(success(2))) { flowOf(success(it * 10)) }.toList()
        assertEquals(listOf(success(2 to 20)), chained)
    }

    @Test
    fun `chainedDataResults stops at an error in the first flow`() = runTest {
        val failure = NotFoundException(message = "missing")
        val chained =
            chainedDataResults(flowOf(DataResult.Error(failure))) { flowOf(success(it)) }.toList()
        assertSame(failure, (chained.single() as DataResult.Error).exception)
    }
}
