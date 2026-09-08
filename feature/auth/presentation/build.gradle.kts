plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.auth.domain)

    testImplementation(testFixtures(projects.feature.auth.domain))
}
