package com.example.androidproject1.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Cache
import java.io.File

/**
 * The real engine, with an on-disk HTTP cache: a GET whose response allows it is served from
 * [cacheDirectory] on a second identical request, instead of a round trip that would answer the
 * same bytes.
 *
 * In `src/main` rather than beside the OkHttp construction in `src/prod`/`src/staging`, which is
 * per flavor and never on `devDebug`'s test classpath — `:app`'s unit tests run for `devDebug`
 * only (see `AndroidApplicationConventionPlugin`), and this is the one piece of the real engine
 * worth a JVM test.
 */
fun cachedOkHttpEngine(cacheDirectory: File, cacheSizeBytes: Long): HttpClientEngine =
    OkHttp.create {
        config {
            cache(Cache(directory = cacheDirectory, maxSize = cacheSizeBytes))
        }
    }
