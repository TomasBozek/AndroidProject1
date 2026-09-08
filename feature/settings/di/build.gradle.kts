plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    // See feature/auth/di: presentation is re-exported, nothing else is.
    api(projects.feature.settings.presentation)

    implementation(projects.feature.auth.domain)
}
