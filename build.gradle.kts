// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// No detekt/ktlint here on purpose: detekt 1.23 embeds a Kotlin compiler that cannot read the
// JDK 25 this build's daemon is pinned to (gradle/gradle-daemon-jvm.properties), and running it
// on a separate toolchain fights AGP 9's plugin ordering. Formatting is handled by .editorconfig
// and correctness by `./gradlew lint`. Revisit when detekt 2.x is stable.
