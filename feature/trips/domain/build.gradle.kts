plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeTripsRepository and FakeDestinationsRepository: presentation's ViewModel tests fake
    // these rather than standing up Room.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
