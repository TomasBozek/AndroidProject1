package com.example.androidproject1

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A cold-start deep link is applied once, not once per activity creation.
 *
 * `onCreate` runs again on every rotation with the same launch intent still attached. Re-reading
 * it there rebuilt the synthesised Home → Categories → category → product path and threw away
 * whatever the user had navigated to since — silently, and only on a real device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = App::class)
class MainActivityDeepLinkTest {

    // `App` starts the real graph, and it lives in the same JVM as every other test in this
    // module — one that calls startKoin itself fails on a graph this test left running.
    @After
    fun tearDown() = stopKoin()

    private fun launchIntent() = Intent(Intent.ACTION_VIEW).apply {
        setClassName(
            androidx.test.core.app.ApplicationProvider.getApplicationContext<App>(),
            MainActivity::class.java.name,
        )
        data = Uri.parse("${BuildConfig.APPLICATION_ID}://product/coffee")
    }

    @Test
    fun `the launch intent is read on the first creation only`() {
        ActivityScenario.launch<MainActivity>(launchIntent()).use { scenario ->
            // The link is pending, or already handed to the back stack and cleared. Either way the
            // activity has read the intent once.
            scenario.onActivity { it.viewModel.onDeepLinkApplied() }

            scenario.recreate()

            // The ViewModel outlives the recreation, so a link re-read from the intent would show
            // up here as keys pending all over again.
            scenario.onActivity { activity ->
                assertEquals(emptyList<Any>(), activity.viewModel.deepLink.value)
            }
        }
    }
}
