plugins {
    alias(libs.plugins.convention.service.core.ui)
}

android {
    // Keeps this module's string names from silently colliding with a consuming app's. The only
    // Android configuration left in a module build file, because it is the only one that is not
    // shared — see the convention plugins in build-logic/ (D64).
    resourcePrefix = "core_"
}

dependencies {
    api(projects.service.core.domain)

    // Re-exported: a module that takes these fixtures for MainDispatcherRule gets FakeLogger too,
    // so a screen test still needs one testFixtures line rather than two.
    testFixturesApi(testFixtures(projects.service.core.domain))

    testImplementation(testFixtures(projects.service.core.domain))
}
