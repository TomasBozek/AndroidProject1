package com.example.androidproject1.core.network

import com.example.androidproject1.core.domain.error.BadRequestError
import com.example.androidproject1.core.domain.error.NetworkError
import com.example.androidproject1.core.domain.error.NotFoundError
import com.example.androidproject1.core.domain.error.ServerError
import com.example.androidproject1.core.domain.error.UnauthorizedError
import com.example.androidproject1.core.domain.error.UnexpectedError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Every row of the status table, driven through a real client on `MockEngine`.
 *
 * Calling `forStatus` directly would test the `when`; going through the client tests what the app
 * actually depends on — that `expectSuccess` raises a `ResponseException` and that the mapper
 * recognises it.
 */
class HttpErrorMapperTest {

    private fun clientReturning(status: HttpStatusCode) = HttpClient(
        MockEngine { respondError(status) },
    ) { expectSuccess = true }

    private suspend fun errorFor(status: HttpStatusCode) = runCatching {
        clientReturning(status).get("https://example.test/")
    }.exceptionOrNull().let { HttpErrorMapper.map(requireNotNull(it)) }

    @Test
    fun `401 and 403 are unauthorized`() = runTest {
        assertTrue(errorFor(HttpStatusCode.Unauthorized) is UnauthorizedError)
        assertTrue(errorFor(HttpStatusCode.Forbidden) is UnauthorizedError)
    }

    @Test
    fun `404 is not found`() = runTest {
        assertTrue(errorFor(HttpStatusCode.NotFound) is NotFoundError)
    }

    @Test
    fun `other 4xx are bad request`() = runTest {
        assertTrue(errorFor(HttpStatusCode.BadRequest) is BadRequestError)
        assertTrue(errorFor(HttpStatusCode.Conflict) is BadRequestError)
        assertTrue(errorFor(HttpStatusCode.UnprocessableEntity) is BadRequestError)
        assertTrue(errorFor(HttpStatusCode.TooManyRequests) is BadRequestError)
    }

    @Test
    fun `5xx are server errors`() = runTest {
        assertTrue(errorFor(HttpStatusCode.InternalServerError) is ServerError)
        assertTrue(errorFor(HttpStatusCode.BadGateway) is ServerError)
        assertTrue(errorFor(HttpStatusCode.ServiceUnavailable) is ServerError)
        assertTrue(errorFor(HttpStatusCode.GatewayTimeout) is ServerError)
    }

    @Test
    fun `a transport failure is a network error, not an unexpected one`() {
        assertTrue(HttpErrorMapper.map(IOException("no route to host")) is NetworkError)
    }

    @Test
    fun `anything else is unexpected`() {
        assertTrue(HttpErrorMapper.map(IllegalStateException("bug")) is UnexpectedError)
    }

    @Test
    fun `cancellation is rethrown, never mapped`() {
        // A cancelled request is not a failure. Mapping it is how a screen shows an error dialog
        // on its way out of composition.
        val thrown = runCatching { HttpErrorMapper.map(CancellationException("left the screen")) }
        assertTrue(thrown.exceptionOrNull() is CancellationException)
    }

    @Test
    fun `a success is not an error at all`() = runTest {
        val client = HttpClient(MockEngine { respond("ok") }) { expectSuccess = true }

        val result = runCatching { client.get("https://example.test/") }

        assertEquals(null, result.exceptionOrNull())
    }
}
