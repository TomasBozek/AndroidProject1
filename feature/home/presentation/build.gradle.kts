plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)

    // Another feature's *domain* — allowed, and the point of feat.1: Home shows favourites without
    // knowing anything about how the catalog draws itself.
    api(projects.feature.catalog.domain)

    // FakeFavouritesRepository, which lives with the interface it fakes.
    testImplementation(testFixtures(projects.feature.catalog.domain))
}
