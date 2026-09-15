package com.example.androidproject1.network

import android.content.Context
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.service.network.ConnectivityMonitor
import io.ktor.client.engine.HttpClientEngine
import java.io.File

/**
 * The real engine.
 *
 * One of these per flavor source set, which is what lets `dev` swap in fixtures (D20) without a
 * runtime flag or a branch in shared code — the wrong build simply does not contain the other.
 */
fun networkEngine(context: Context, cacheSizeBytes: Long): HttpClientEngine =
    cachedOkHttpEngine(cacheDirectory = File(context.cacheDir, "http"), cacheSizeBytes = cacheSizeBytes)

/** The platform's own answer to "is there a route": see [AndroidConnectivityMonitor] (D68). */
fun connectivityMonitor(context: Context, dispatchers: DispatcherProvider): ConnectivityMonitor =
    AndroidConnectivityMonitor(context, dispatchers)
