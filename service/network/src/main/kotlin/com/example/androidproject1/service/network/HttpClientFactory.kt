package com.example.androidproject1.service.network

import com.example.androidproject1.service.core.domain.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * Builds the app's [HttpClient].
 *
 * The engine is a parameter, not a dependency: `:app` hands it OkHttp, `dev` hands it the
 * `MockEngine` serving D20's fixtures, and a test hands it one that answers whatever the test is
 * about. That is the whole reason this module has no engine of its own.
 *
 * The client retries a failed request itself, so no data source has to: up to
 * [NetworkConfig.retries] more tries on a 5xx or a transport failure, with an exponential backoff.
 * **A POST is never retried** — a retried order is a double order, and the client cannot tell a
 * request that never arrived from one whose answer did not come back. Only the idempotent methods
 * are, which is what makes the policy safe to apply to every call in the app at once.
 */
object HttpClientFactory {

    /** Lenient on unknown keys: a server adding a field must not break a shipped client. */
    val DefaultJson: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun create(
        engine: HttpClientEngine,
        config: NetworkConfig,
        logger: Logger? = null,
        tokenStore: TokenStore? = null,
        tokenRefresher: TokenRefresher? = null,
        json: Json = DefaultJson,
    ): HttpClient = HttpClient(engine) {
        // A non-2xx becomes a ResponseException, which HttpErrorMapper turns into a DomainError.
        expectSuccess = true

        install(ContentNegotiation) { json(json) }

        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMillis
            connectTimeoutMillis = config.connectTimeoutMillis
            socketTimeoutMillis = config.socketTimeoutMillis
        }

        if (config.retries > 0) {
            install(HttpRequestRetry) {
                maxRetries = config.retries

                // Idempotent methods only. A retried GET costs a second read; a retried POST is a
                // second order, a second payment, a second message — and the client cannot tell
                // whether the first one reached the server, only that the answer did not come
                // back. A POST that is safe to repeat says so with an idempotency key and a
                // `retry { }` block on the request itself, which is a decision per endpoint.
                retryIf { request, response ->
                    request.method.isIdempotent() && response.status.value in 500..599
                }
                retryOnExceptionIf { request, cause ->
                    request.method.isIdempotent() && cause.isTransport()
                }

                // 500 ms, then 1 s, then 2 s, plus up to 250 ms of jitter so a fleet coming back
                // from an outage does not arrive in one wave. Capped, because a phone in a tunnel
                // is better served by an error than by a spinner that lasts a minute.
                exponentialDelay(baseDelayMs = 500, maxDelayMs = 5_000, randomizationMs = 250)
            }
        }

        if (logger != null) {
            install(Logging) {
                this.logger = object : KtorLogger {
                    override fun log(message: String) = logger.d { message }
                }
                // HEADERS and BODY carry the bearer token and whatever the user typed, so the
                // level is a decision the caller makes per build type, never a default.
                level = if (config.logBodies) LogLevel.BODY else LogLevel.INFO
            }
        }

        if (tokenStore != null && tokenRefresher != null) {
            val refresher = SingleFlightTokenRefresher(tokenStore, tokenRefresher)
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStore.accessToken()?.let {
                            BearerTokens(accessToken = it, refreshToken = tokenStore.refreshToken())
                        }
                    }
                    refreshTokens {
                        refresher.refresh(staleAccessToken = oldTokens?.accessToken)?.let {
                            BearerTokens(accessToken = it.accessToken, refreshToken = it.refreshToken)
                        }
                    }
                }
            }
        }

        defaultRequest {
            url(config.baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
}

/**
 * The methods RFC 9110 calls idempotent — repeating one has the same effect as making it once.
 *
 * An allowlist rather than "not POST", so a method nobody thought about is not retried by default.
 */
private val IDEMPOTENT_METHODS = setOf(
    HttpMethod.Get,
    HttpMethod.Head,
    HttpMethod.Options,
    HttpMethod.Put,
    HttpMethod.Delete,
)

private fun HttpMethod.isIdempotent(): Boolean = this in IDEMPOTENT_METHODS

/**
 * A failure that never got an answer out of the server, and so is worth asking again.
 *
 * Timeouts are not: the request may well have arrived and been acted on, the wait has already
 * cost the configured seconds, and retrying spends them twice more. Cancellation is not either —
 * the caller has left.
 */
private fun Throwable.isTransport(): Boolean = when (this) {
    is CancellationException -> false
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> false
    else -> this is IOException
}
