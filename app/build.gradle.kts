plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.androidproject1"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.androidproject1"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // R8 on from the start: the keep-rule surface is one screen big today and grows with
            // every reflection-based library. Rules live in src/main/keepRules/.
            optimization {
                enable = true
            }
        }
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
        checkDependencies = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Pulls in every core layer and every feature's `di` module, and through them the whole graph.
    // `implementation`, not `api`: nothing consumes the application module.
    implementation(projects.core.di)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)

    // No androidTest source set: UI tests run under Robolectric as unit tests, so CI needs no
    // emulator. See plan item 4.3.
    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.bundles.testing)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
}
