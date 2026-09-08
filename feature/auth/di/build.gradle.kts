plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    api(projects.feature.auth.data)
    api(projects.feature.auth.domain)
    api(projects.feature.auth.presentation)
}
