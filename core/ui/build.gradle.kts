plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.androidproject1.core.ui"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        // Shared configuration; see lint.xml at the repo root.
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = false
        abortOnError = true
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Re-exported so a feature's `presentation` module needs only `api(projects.core.ui)` to get
    // both this app's theme and the reusable architecture in `:service:core:ui`.
    api(projects.service.core.ui)

    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
