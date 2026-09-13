plugins {
    alias(libs.plugins.convention.feature.presentation)
}

dependencies {
    api(projects.core.ui)
    api(projects.feature.inventory.domain)

    // FakeInventoryRepository, for the ViewModel tests.
    testImplementation(testFixtures(projects.feature.inventory.domain))
}
