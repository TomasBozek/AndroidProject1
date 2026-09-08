package com.example.androidproject1.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Records which classes and methods the app touches on the way to its first screen, so ART can
 * compile them ahead of time instead of interpreting them on a cold start.
 *
 * The flow deliberately stops at the first screen the user actually sees. A profile that walks the
 * whole app is bigger, slower to install and no better at the thing it is for — the first frame.
 *
 * `./gradlew :app:generateDevReleaseBaselineProfile` with a device attached; the result is written
 * to `app/src/dev/generated/baselineProfiles/` and committed.
 */
class StartupBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(packageName = PACKAGE) {
        pressHome()
        startActivityAndWait()

        // Wait for something the app drew rather than a fixed sleep: a sleep long enough for a
        // slow machine is wasted on every fast one, and one that is too short records a profile
        // of the splash screen.
        device.wait(Until.hasObject(By.res(LOGIN_SCREEN)), TIMEOUT_MS)
    }

    private companion object {

        const val PACKAGE = "com.example.androidproject1.dev"

        // The testTag convention from 3.6, doing a second job: AppScaffold publishes it as a
        // resource id, so the profile generator can wait on it.
        const val LOGIN_SCREEN = "LoginScreen"
        const val TIMEOUT_MS = 10_000L
    }
}
