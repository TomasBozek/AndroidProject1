plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    api(projects.service.core.data)
    api(projects.core.ui)

    api(projects.feature.auth.di)
    api(projects.feature.home.di)
    api(projects.feature.settings.di)
    api(projects.feature.launch.di)
    api(projects.feature.catalog.di)
}
