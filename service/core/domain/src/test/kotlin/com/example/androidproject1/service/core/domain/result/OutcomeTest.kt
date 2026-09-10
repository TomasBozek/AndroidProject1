package com.example.androidproject1.service.core.domain.result

import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.error.NotFoundError
import com.example.androidproject1.service.core.domain.error.UnexpectedError
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
}
