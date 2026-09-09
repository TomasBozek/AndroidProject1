plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    // The session row. Another feature's `domain` is fair game; its `presentation` is not.
    api(projects.feature.auth.domain)

    testImplementation(testFixtures(projects.feature.auth.domain))
}
