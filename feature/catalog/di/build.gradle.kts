plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.androidproject1.feature.catalog.di"

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
}

dependencies {
    api(projects.feature.catalog.data)
    api(projects.feature.catalog.domain)
    api(projects.feature.catalog.gateway)
    api(projects.feature.catalog.presentation)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
}
