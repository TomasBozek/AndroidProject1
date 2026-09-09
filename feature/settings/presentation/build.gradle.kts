plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.settings.domain)
    // Another feature's `domain` is fair game; its `presentation` is not — see doctor.py.
    api(projects.feature.auth.domain)

    testImplementation(testFixtures(projects.feature.auth.domain))
    testImplementation(testFixtures(projects.feature.settings.domain))
}
