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
 * to `app/src/devRelease/generated/baselineProfiles/` and committed.
 */
class StartupBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(packageName = PACKAGE) {
        pressHome()
        // Same reason as the benchmark's: a collection run must start from the same place every
        // time, and `collect` repeats this block.
        device.executeShellCommand("pm clear $PACKAGE")
        startActivityAndWait()

        // Wait for something the app drew rather than a fixed sleep: a sleep long enough for a
        // slow machine is wasted on every fast one, and one that is too short records a profile
        // of the splash screen.
        device.wait(Until.hasObject(By.res(FIRST_SCREEN)), TIMEOUT_MS)
    }

    private companion object {

        const val PACKAGE = "com.example.androidproject1.dev"

        // The testTag convention from 3.6, doing a second job: AppScaffold publishes it as a
        // resource id, so the profile generator can wait on it. The first screen of a fresh
        // install is the tour, not the login form — see `StartupBenchmark.FIRST_SCREEN`.
        const val FIRST_SCREEN = "OnboardingScreen"
        const val TIMEOUT_MS = 10_000L
    }
}
