plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.androidproject1.core.di"

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
    api(projects.service.core.data)
    api(projects.core.ui)

    api(projects.feature.auth.di)
    api(projects.feature.home.di)
    api(projects.feature.settings.di)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
}
