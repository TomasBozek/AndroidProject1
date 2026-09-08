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

    lint {
        // Shared configuration; see lint.xml at the repo root.
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = false
        abortOnError = true
    }

    buildFeatures {
        compose = true
    }

    // Ships MainDispatcherRule to every module that tests a ViewModel, so none of them re-writes
    // Dispatchers.setMain/resetMain. Consume with `testImplementation(testFixtures(projects...))`.
    testFixtures {
        enable = true
    }
}

dependencies {
    api(projects.service.core.domain)

    api(libs.androidx.activity.compose)

    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    // SavedStateHandle + toRoute(): BaseViewModel reads typed navigation arguments.
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.androidx.navigation.compose)

    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.bundles.testing)
}
