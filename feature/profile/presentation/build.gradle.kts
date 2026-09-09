plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.profile.domain)
}
