// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    // Advisory, not a gate: `./gradlew buildHealth` reports dependencies declared but unused, used
    // but undeclared, and `api` where `implementation` would do. Failing the build on it would
    // make every dependency edit a negotiation with a heuristic.
    alias(libs.plugins.dependency.analysis)
}

subprojects {
    apply(plugin = "com.autonomousapps.dependency-analysis")
}

// No detekt/ktlint here on purpose: detekt 1.23 embeds a Kotlin compiler that cannot read the
// JDK 25 this build's daemon is pinned to (gradle/gradle-daemon-jvm.properties), and running it
// on a separate toolchain fights AGP 9's plugin ordering. Formatting is handled by .editorconfig
// and correctness by `./gradlew lint`. Revisit when detekt 2.x is stable.
