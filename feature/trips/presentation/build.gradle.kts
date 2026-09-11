plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.trips.domain)

    // The fakes live with the interfaces they fake.
    testImplementation(testFixtures(projects.feature.trips.domain))
}
