plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)

    // Another feature's *domain* — allowed, and the point of feat.1: Home shows favourites without
    // knowing anything about how the catalog draws itself.
    api(projects.feature.catalog.domain)
    // The same rule, for the inventory count: Home says how many things are kept without knowing
    // what the list looks like.
    api(projects.feature.inventory.domain)

    // FakeFavouritesRepository, which lives with the interface it fakes.
    testImplementation(testFixtures(projects.feature.catalog.domain))
    testImplementation(testFixtures(projects.feature.inventory.domain))
}
