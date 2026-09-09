package com.example.androidproject1.core.network

import com.example.androidproject1.core.domain.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * Builds the app's [HttpClient].
 *
 * The engine is a parameter, not a dependency: `:app` hands it OkHttp, `dev` hands it the
 * `MockEngine` serving D20's fixtures, and a test hands it one that answers whatever the test is
 * about. That is the whole reason this module has no engine of its own.
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
