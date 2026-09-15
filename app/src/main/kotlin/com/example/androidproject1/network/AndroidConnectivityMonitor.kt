package com.example.androidproject1.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.example.androidproject1.service.core.domain.coroutines.DispatcherProvider
import com.example.androidproject1.service.network.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

/**
 * [ConnectivityMonitor] over the platform's default network.
 *
 * The callback is registered on the first collector and unregistered after the last one leaves —
 * `callbackFlow` into `stateIn(WhileSubscribed)` — so an app nobody is looking at holds none.
 * What is reported is whether a default network exists at all; whether it reaches the internet
 * (a captive portal says yes to the first and no to the second) is left to the request, which
 * fails and says so. Between collectors [online] keeps its last value, which is what the retry
 * policy reads.
 *
 * In `src/main` for the reason `cachedOkHttpEngine` is: `:app`'s unit tests run for `devDebug`
 * only, and this is the piece worth a Robolectric test.
 */
class AndroidConnectivityMonitor(
    context: Context,
    dispatchers: DispatcherProvider,
) : ConnectivityMonitor {

    private val manager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override val online: StateFlow<Boolean> = callbackFlow {
        // The current reading first: the platform reports a network that is there through
        // `onAvailable` as soon as the callback registers, but says nothing when there is none.
        trySend(manager.activeNetwork != null)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + dispatchers.default),
        started = SharingStarted.WhileSubscribed(),
        initialValue = manager.activeNetwork != null,
    )
}
