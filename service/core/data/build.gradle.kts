plugins {
    alias(libs.plugins.convention.service.core.data)
}

dependencies {
    api(projects.service.core.domain)
}
