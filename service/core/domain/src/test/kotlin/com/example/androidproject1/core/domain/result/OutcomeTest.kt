package com.example.androidproject1.core.domain.result

import com.example.androidproject1.core.domain.error.DomainError
import com.example.androidproject1.core.domain.error.NotFoundError
import com.example.androidproject1.core.domain.error.UnexpectedError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class OutcomeTest {

    private fun <T> success(value: T) = Outcome.Success(value)

    private fun failure(error: DomainError = NotFoundError()) = Outcome.Failure(error)

    // --- map ---

    @Test
    fun `map transforms the success value`() {
        assertEquals(success(2), success(1).map { it + 1 })
    }

    @Test
    fun `map passes an error through untouched`() {
        val original = failure()
        assertSame(original.error, (original.map { it } as Outcome.Failure).error)
    }

    @Test
    fun `map wraps an unexpected throwable as an unexpected error`() {
        val result = success(1).map<Int, Int> { throw IllegalStateException("boom") }
        val error = (result as Outcome.Failure).error
        assertTrue(error is UnexpectedError)
        assertTrue(error.cause is IllegalStateException)
    }

    @Test
    fun `map keeps a thrown domain error as it is`() {
        val thrown = NotFoundError(message = "gone")
        val result = success(1).map<Int, Int> { throw thrown }
        assertSame(thrown, (result as Outcome.Failure).error)
    }

    /**
     * Regression: `runCatching` catches [CancellationException] too, so without an explicit rethrow
     * a cancelled coroutine would be reported to the user as an unexpected error.
     */
    @Test
    fun `map rethrows cancellation instead of reporting an error`() {
        assertThrows(CancellationException::class.java) {
            success(1).map<Int, Int> { throw CancellationException("cancelled") }
        }
    }

    // --- flatMap / recover ---

    @Test
    fun `flatMap chains into the next result`() {
        assertEquals(success("1"), success(1).flatMap { success(it.toString()) })
    }

    @Test
    fun `flatMap rethrows cancellation`() {
        assertThrows(CancellationException::class.java) {
            success(1).flatMap<Int, Int> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `recover can recover an error into a success`() {
        assertEquals(success(0), failure().recover { success(0) })
    }

    @Test
    fun `recover leaves a success alone`() {
        assertEquals(success(1), success(1).recover { success(0) })
    }

    @Test
    fun `recover rethrows cancellation`() {
        assertThrows(CancellationException::class.java) {
            failure().recover<Int> { throw CancellationException("cancelled") }
        }
    }

    // --- combining ---

    @Test
    fun `combineOutcomes pairs two successes`() = runTest {
        val combined = combineOutcomes(flowOf(success(1)), flowOf(success("a"))).toList()
        assertEquals(listOf(success(1 to "a")), combined)
    }

    @Test
    fun `combineOutcomes short-circuits on the first error`() = runTest {
        val notFound = NotFoundError(message = "missing")
        val combined =
            combineOutcomes(flowOf(Outcome.Failure(notFound)), flowOf(success("a"))).toList()
        assertSame(notFound, (combined.single() as Outcome.Failure).error)
    }

    @Test
    fun `chainOutcomes feeds the first value into the second flow`() = runTest {
        val chained = chainOutcomes(flowOf(success(2))) { flowOf(success(it * 10)) }.toList()
        assertEquals(listOf(success(2 to 20)), chained)
    }

    @Test
    fun `chainOutcomes stops at an error in the first flow`() = runTest {
        val notFound = NotFoundError(message = "missing")
        val chained =
            chainOutcomes(flowOf(Outcome.Failure(notFound))) { flowOf(success(it)) }.toList()
        assertSame(notFound, (chained.single() as Outcome.Failure).error)
    }
}
