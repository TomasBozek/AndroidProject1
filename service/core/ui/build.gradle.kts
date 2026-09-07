plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.androidproject1.service.core.ui"
    // Keeps this module's string names from silently colliding with a consuming app's.
    resourcePrefix = "core_"

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

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.service.core.domain)

    api(libs.androidx.activity.compose)

    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
