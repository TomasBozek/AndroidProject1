plugins {
    alias(libs.plugins.convention.feature.di)
}

dependencies {
    api(projects.feature.template.data)
    api(projects.feature.template.domain)
    api(projects.feature.template.presentation)
}
