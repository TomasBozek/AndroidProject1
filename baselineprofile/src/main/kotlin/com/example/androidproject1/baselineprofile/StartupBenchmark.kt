package com.example.androidproject1.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Cold start, with and without the profile, so the profile's value is a number rather than a
 * belief.
 *
 * **Run this on a physical device.** An emulator shares a CPU with everything else on the machine
 * and its numbers move with what else is running; the benchmark refuses to run on one unless
 * errors are suppressed, and that refusal is correct.
 *
 * `./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`
 */
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupWithoutProfile() = startup(CompilationMode.None())

    @Test
    fun startupWithProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = ITERATIONS,
        // `StartupMode.COLD` force-stops the app but leaves its data, so which screen it opens on
        // would depend on whatever the last run left behind — onboarding seen or not, signed in or
        // not. Clearing it makes every iteration the same first launch, which is the start that
        // the profile is worth the most on.
        setupBlock = {
            pressHome()
            device.executeShellCommand("pm clear $PACKAGE")
        },
    ) {
        startActivityAndWait()
        // The same wait the profile generator uses: measure to the first frame the user sees, not
        // to the splash screen.
        device.wait(Until.hasObject(By.res(FIRST_SCREEN)), TIMEOUT_MS)
    }

    private companion object {

        const val PACKAGE = "com.example.androidproject1.dev"

        /**
         * The first screen of a fresh install, which is the tour rather than the login form: the
         * stored `seen` flag is unset, so `MainViewModel` routes to onboarding. Waiting on
         * `LoginScreen` here is what this test used to do, and after the onboarding flow landed it
         * meant every iteration waited out [TIMEOUT_MS] for a screen that never appeared.
         */
        const val FIRST_SCREEN = "OnboardingScreen"
        const val ITERATIONS = 10
        const val TIMEOUT_MS = 10_000L
    }
}
