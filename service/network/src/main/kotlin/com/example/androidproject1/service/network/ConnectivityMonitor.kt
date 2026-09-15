package com.example.androidproject1.service.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the device has a route to the outside world at all.
 *
 * A port, like the engine: this module is Kotlin/JVM and cannot ask `ConnectivityManager`, so
 * `:app` implements it over the platform, `dev` over its fixture switch, and the client only reads
 * it — the retry policy gives up at once while [online] is false, because a phone with no route is
 * not helped by three tries and two seconds of backoff (D68). A screen never reads it: the banner
 * `MainActivity` draws is the one place the user is told.
 */
interface ConnectivityMonitor {

    /** `true` while a request has somewhere to go. A `StateFlow`, so it can be both collected and read. */
    val online: StateFlow<Boolean>

    /** For a client, or a test, with nothing to observe: behaves as if never told. */
    object AlwaysOnline : ConnectivityMonitor {

        override val online: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    }
}
