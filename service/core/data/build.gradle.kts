plugins {
    alias(libs.plugins.convention.feature.data)
}

dependencies {
    api(projects.service.core.domain)
    api(libs.androidx.datastore.preferences)

    testImplementation(testFixtures(projects.service.core.domain))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
