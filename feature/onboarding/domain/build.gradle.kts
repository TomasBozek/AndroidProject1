plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeOnboardingRepository: the tour writes the flag and `MainViewModel` reads it, so
    // :app's tests need the same fake this feature's do.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
