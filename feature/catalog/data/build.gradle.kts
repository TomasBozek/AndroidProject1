plugins {
    alias(libs.plugins.convention.feature.data)
}

dependencies {
    api(projects.service.core.data)
    api(projects.feature.catalog.domain)
}
