plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.androidproject1.feature.template.presentation"

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

    testOptions {
        unitTests {
            // The TemplateArgs screen's test decodes a route, which needs a real android.os.Bundle.
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.template.domain)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)

    // The seventh file of every screen is its ViewModel test; MainDispatcherRule comes from
    // :service:core:ui's test fixtures so no module rewrites Dispatchers.setMain.
    testImplementation(libs.bundles.testing)
    testImplementation(libs.robolectric)
    testImplementation(testFixtures(projects.service.core.ui))
}
