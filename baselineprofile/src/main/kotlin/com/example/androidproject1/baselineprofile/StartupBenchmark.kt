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
 * `./gradlew :baselineprofile:connectedBenchmarkAndroidTest`
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
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        // The same wait the profile generator uses: measure to the first frame the user sees, not
        // to the splash screen.
        device.wait(Until.hasObject(By.res(LOGIN_SCREEN)), TIMEOUT_MS)
    }

    private companion object {

        const val PACKAGE = "com.example.androidproject1.dev"
        const val LOGIN_SCREEN = "LoginScreen"
        const val ITERATIONS = 10
        const val TIMEOUT_MS = 10_000L
    }
}
