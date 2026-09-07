plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.androidproject1.feature.example.di"

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
    api(projects.feature.example.data)
    api(projects.feature.example.domain)
    api(projects.feature.example.infrastructure)
    api(projects.feature.example.presentation)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
}
