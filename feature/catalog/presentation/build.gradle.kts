plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.catalog.domain)

    // FakeFavouritesRepository, which lives with the interface it fakes.
    testImplementation(testFixtures(projects.feature.catalog.domain))
}
