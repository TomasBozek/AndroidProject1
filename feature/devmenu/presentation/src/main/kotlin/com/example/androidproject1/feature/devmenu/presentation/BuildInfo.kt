package com.example.androidproject1.feature.devmenu.presentation

import androidx.compose.runtime.Immutable

/**
 * What this build is, as `:app` knows it.
 *
 * A value handed in rather than read here: `BuildConfig` belongs to the application module, and a
 * feature that reached for it would stop being a feature. `:app` binds one of these in its own
 * Koin module — the same reason `NetworkConfig` is assembled there.
 */
@Immutable
data class BuildInfo(
    val applicationId: String,
    val flavor: String,
    val buildType: String,
    val versionName: String,
    val versionCode: Int,
    val baseUrl: String,
    /**
     * Whether a leak watcher is on the classpath of this build.
     *
     * Defaulted, and it has to be: Koin's `verify()` reads the primary constructor even of a type
     * bound by a lambda, and a `Boolean` parameter without a default is reported as a missing
     * binding. See `KoinGraphTest`.
     */
    val leakDetection: Boolean = false,
) {

    companion object {

        val PREVIEW = BuildInfo(
            applicationId = "com.example.androidproject1.dev",
            flavor = "dev",
            buildType = "debug",
            versionName = "1.0",
            versionCode = 1,
            baseUrl = "https://dev.example.com/",
            leakDetection = true,
        )
    }
}
