package com.example.androidproject1.network

import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/**
 * The real engine.
 *
 * One of these per flavor source set, which is what lets `dev` swap in fixtures (D20) without a
 * runtime flag or a branch in shared code — the wrong build simply does not contain the other.
 */
fun networkEngine(context: Context): HttpClientEngine = OkHttp.create()
