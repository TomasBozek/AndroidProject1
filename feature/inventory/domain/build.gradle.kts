plugins {
    alias(libs.plugins.convention.kotlin.jvm)
    // FakeInventoryRepository: presentation's ViewModel tests fake this rather than standing up
    // Room.
    `java-test-fixtures`
}

dependencies {
    api(projects.service.core.domain)
}
