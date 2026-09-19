plugins {
    alias(libs.plugins.convention.feature.data)
    alias(libs.plugins.convention.android.room)
}

dependencies {
    api(projects.service.core.data)
    api(projects.feature.movies.domain)
    api(projects.service.network)
}
