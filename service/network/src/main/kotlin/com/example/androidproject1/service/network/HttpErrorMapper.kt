package com.example.androidproject1.service.network

import com.example.androidproject1.service.core.domain.error.BadRequestError
import com.example.androidproject1.service.core.domain.error.DomainError
import com.example.androidproject1.service.core.domain.error.NetworkError
import com.example.androidproject1.service.core.domain.error.NotFoundError
import com.example.androidproject1.service.core.domain.error.ServerError
import com.example.androidproject1.service.core.domain.error.UnauthorizedError
import com.example.androidproject1.service.core.domain.error.UnexpectedError
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * The one place an HTTP failure becomes a [DomainError].
 *
 * Everything above the data layer reasons about `UnauthorizedError`, not about 401 — so the status
 * table lives here rather than being re-read in each repository.
 */
object HttpErrorMapper {

    /**
     * Maps a thrown exception to its domain equivalent.
     *
     * Cancellation is rethrown rather than mapped: a cancelled request is not a failure, and
     * turning it into one is how a screen ends up showing an error dialog on its way out.
     */
    fun map(throwable: Throwable): DomainError = when (throwable) {
        is CancellationException -> throw throwable
        is DomainError -> throwable
        is ResponseException -> forStatus(throwable.response.status, throwable)
        // A timeout never reached a server, so it is the same condition as no connection.
        is TimeoutCancellationException -> NetworkError(cause = throwable)
        is IOException -> NetworkError(cause = throwable)
        else -> UnexpectedError(cause = throwable)
    }

    fun forStatus(status: HttpStatusCode, cause: Throwable? = null): DomainError = when (status.value) {
        HttpStatusCode.Unauthorized.value, HttpStatusCode.Forbidden.value ->
            UnauthorizedError(message = status.description, cause = cause)

        HttpStatusCode.NotFound.value ->
            NotFoundError(message = status.description, cause = cause)

        // Every other 4xx is the client's fault: a malformed request, a validation failure, a
        // conflict. They differ in what the body says, not in what the app can do about them.
        in CLIENT_ERRORS -> BadRequestError(message = status.description, cause = cause)

        in SERVER_ERRORS -> ServerError(message = status.description, cause = cause)

        else -> UnexpectedError(message = status.description, cause = cause)
    }

    private val CLIENT_ERRORS = 400..499
    private val SERVER_ERRORS = 500..599
}
