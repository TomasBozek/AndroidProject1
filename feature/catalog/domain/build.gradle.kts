plugins {
    alias(libs.plugins.convention.android.library)
}

dependencies {
    api(projects.service.core.domain)
}
