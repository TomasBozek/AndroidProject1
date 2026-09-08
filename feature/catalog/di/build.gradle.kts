plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    api(projects.feature.catalog.data)
    api(projects.feature.catalog.domain)
    api(projects.feature.catalog.presentation)
}
