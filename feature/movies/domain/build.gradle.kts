plugins {
    alias(libs.plugins.convention.kotlin.jvm)
}

dependencies {
    api(projects.service.core.domain)
}
