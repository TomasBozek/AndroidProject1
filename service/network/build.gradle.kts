plugins {
    alias(libs.plugins.convention.service.network)
}

dependencies {
    api(projects.service.core.domain)
}
