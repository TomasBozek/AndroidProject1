plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.androidproject1.feature.template.di"

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
    api(projects.feature.template.data)
    api(projects.feature.template.domain)
    api(projects.feature.template.gateway)
    api(projects.feature.template.presentation)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin.android)
}
