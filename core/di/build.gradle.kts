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

    lint {
        // Shared configuration; see lint.xml at the repo root.
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = false
        abortOnError = true
    }
}

dependencies {
    api(projects.service.core.data)
    api(projects.core.ui)

    api(projects.feature.auth.di)
    api(projects.feature.home.di)
    api(projects.feature.settings.di)
    api(projects.feature.launch.di)
    api(projects.feature.catalog.di)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
}
