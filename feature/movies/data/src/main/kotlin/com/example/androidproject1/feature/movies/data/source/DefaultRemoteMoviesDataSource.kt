package com.example.androidproject1.feature.movies.data.source

import com.example.androidproject1.feature.movies.domain.MovieDetail
import com.example.androidproject1.feature.movies.domain.MoviePage
import com.example.androidproject1.feature.movies.domain.TmdbConfig
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.service.network.HttpErrorMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.withContext

/**
 * TMDB over the shared client. The URLs are absolute (D80): the client's `defaultRequest` names the
 * template's own `BASE_URL`, and Ktor leaves a request that brings its own host alone, so one
 * client serves both servers. The key rides as `api_key`, TMDB's v3 way, so the auth plugin's
 * bearer token — the session's, for the other host — is not in the way.
 *
 * Switching to IO is this class's job, not the repository's: `BaseRepository` runs on the caller's
 * context, and the caller is `viewModelScope` — which is `Dispatchers.Main`. Failures are mapped
 * here so the repository above sees a `DomainError` and never an HTTP status.
 */
class DefaultRemoteMoviesDataSource(
    private val client: HttpClient,
    private val dispatcherProvider: DispatcherProvider,
    private val config: TmdbConfig,
) : RemoteMoviesDataSource {

    override suspend fun getPopular(page: Int): MoviePage = request {
        client.get(config.apiBaseUrl + "movie/popular") {
            parameter("page", page)
            parameter("api_key", config.apiKey)
        }.body<MoviePageDto>().toDomain(config.imageBaseUrl)
    }

    override suspend fun getMovie(id: Int): MovieDetail = request {
        client.get(config.apiBaseUrl + "movie/$id") {
            parameter("api_key", config.apiKey)
        }.body<MovieDetailDto>().toDomain(config.imageBaseUrl)
    }

    private suspend fun <T> request(block: suspend () -> T): T = withContext(dispatcherProvider.io) {
        try {
            block()
        } catch (throwable: Throwable) {
            throw HttpErrorMapper.map(throwable)
        }
    }
}
