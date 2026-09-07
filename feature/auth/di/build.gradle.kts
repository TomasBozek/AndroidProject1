plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.androidproject1.feature.auth.di"

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
}

dependencies {
    api(projects.feature.auth.data)
    api(projects.feature.auth.domain)
    api(projects.feature.auth.infrastructure)
    api(projects.feature.auth.presentation)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
}
