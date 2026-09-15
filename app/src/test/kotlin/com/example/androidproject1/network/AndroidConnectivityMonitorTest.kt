package com.example.androidproject1.network

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import com.example.androidproject1.service.core.domain.test.TestDispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork

/** The platform half of the port, driven through Robolectric's `ConnectivityManager`. */
@RunWith(RobolectricTestRunner::class)
class AndroidConnectivityMonitorTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = shadowOf(context.getSystemService(ConnectivityManager::class.java))

    @Test
    fun `follows the default network while collected, and holds no callback after`() = runTest {
        val monitor = AndroidConnectivityMonitor(context, TestDispatchers)
        val seen = mutableListOf<Boolean>()

        val collector = launch { monitor.online.collect { seen += it } }
        advanceUntilIdle()
        val callback = manager.networkCallbacks.single()
        // Drained between the two: a StateFlow conflates, and a collector that wakes after both
        // would see only the second — which equals the first and so shows as nothing at all.
        callback.onLost(ShadowNetwork.newInstance(1))
        advanceUntilIdle()
        callback.onAvailable(ShadowNetwork.newInstance(1))
        advanceUntilIdle()

        // Robolectric's default is a connected network, so the first reading is online.
        assertEquals(listOf(true, false, true), seen)

        collector.cancel()
        advanceUntilIdle()
        assertTrue(manager.networkCallbacks.isEmpty())
    }
}
