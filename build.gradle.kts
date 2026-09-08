// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.screenshot) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    // Advisory, not a gate: `./gradlew buildHealth` reports dependencies declared but unused, used
    // but undeclared, and `api` where `implementation` would do. Failing the build on it would
    // make every dependency edit a negotiation with a heuristic.
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.ktlint)
}

// Real modules only. `:feature` and `:service:core` are path segments with no build file, and
// applying a plugin to one makes Gradle materialise a build directory for it.
subprojects {
    if (!buildFile.isFile) return@subprojects
    apply(plugin = "com.autonomousapps.dependency-analysis")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

// ktlint runs here; detekt still does not. Re-tested 2026-09-08 on detekt 1.23.8, the current
// release: its embedded Kotlin compiler rejects the JDK 25 this build's daemon is pinned to
// (gradle/gradle-daemon-jvm.properties) — first refusing `--jvm-target 25`, then failing on the
// version string itself once that is pinned to 17. detekt 2.x is still 2.0.0-alpha, and this
// project takes stable. ktlint has no such problem: it reads .editorconfig, so the rule set lives
// there rather than in a second config file. `./gradlew ktlintCheck` gates CI; `ktlintFormat`
// fixes. Revisit detekt when 2.x is stable.
